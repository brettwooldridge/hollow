/*
 *
 *  Copyright 2016 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.netflix.hollow.api.producer;

import java.io.IOException;
import java.util.EventListener;

import com.netflix.hollow.api.HollowStateTransition;
import com.netflix.hollow.api.client.HollowBlobRetriever;
import com.netflix.hollow.api.client.HollowClient;
import com.netflix.hollow.api.consumer.HollowAnnouncementRetriever;
import com.netflix.hollow.api.consumer.HollowConsumer;
import com.netflix.hollow.api.consumer.HollowConsumer.ReadState;
import com.netflix.hollow.core.read.engine.HollowReadStateEngine;
import com.netflix.hollow.core.write.HollowBlobWriter;
import com.netflix.hollow.core.write.HollowWriteStateEngine;
import com.netflix.hollow.core.write.objectmapper.HollowObjectMapper;

/**
 *  Beta API subject to change.
 * 
 * @author Tim Taylor {@literal<timt@netflix.com>}
 */
public class HollowProducer {
    private final VersionMinter versionMinter;
    private final HollowPublisher publisher;
    private final HollowProducer.Validator validator;
    private final HollowAnnouncer announcer;
    private final HollowAnnouncementRetriever announcementRetriever;
    private final HollowBlobRetriever blobRetriever;
    private final HollowWriteStateEngine writeEngine;
    private final HollowObjectMapper objectMapper;

    private HollowStateTransition announced;

    public HollowProducer(VersionMinter versionMinter, HollowPublisher publisher, HollowAnnouncer announcer) {
        this(versionMinter, publisher, announcer, HollowAnnouncementRetriever.NO_ANNOUNCEMENTS, null);
    }

    public HollowProducer(VersionMinter versionMinter,
            HollowPublisher publisher,
            HollowAnnouncer announcer, 
            HollowAnnouncementRetriever announcementRetriever,
            HollowBlobRetriever blobRetriever) {
        this.versionMinter = versionMinter;
        this.publisher = publisher;
        this.validator = Validator.NO_VALIDATIONS;
        this.announcer = announcer;
        this.announcementRetriever = announcementRetriever;
        this.blobRetriever = blobRetriever;

        writeEngine = new HollowWriteStateEngine();
        objectMapper = new HollowObjectMapper(writeEngine);
        announced = new HollowStateTransition();
    }

    public void initializeDataModel(Class<?>...classes) {
        for(Class<?> c : classes) 
            objectMapper.initializeTypeState(c);
    }

    public HollowProducer restore() {
        try {
            System.out.println("RESTORE PRIOR STATE...");
            long stateVersion = announcementRetriever.get();
            if(stateVersion != Long.MIN_VALUE) {
                // TODO: timt: use HollowConsumer
                HollowClient client = new HollowClient(blobRetriever);
                client.triggerRefreshTo(stateVersion);
                restoreFrom(client.getStateEngine(), client.getCurrentVersionId());
                System.out.format("RESUMING DELTA CHAIN AT %s\n", client.getCurrentVersionId());
            } else {
                System.out.println("RESTORE UNAVAILABLE; PRODUCING NEW DELTA CHAIN");
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            System.out.println("RESTORE UNAVAILABLE; PRODUCING NEW DELTA CHAIN");
        }
        return this;
    }

    public HollowProducer restoreFrom(HollowReadStateEngine priorAnnouncedState, long priorAnnouncedVersion) {
        writeEngine.restoreFrom(priorAnnouncedState);
        announced = new HollowStateTransition(priorAnnouncedVersion);
        return this;
    }

    /**
     * Each cycle produces a single state.
     */
    public void runCycle(Populator task) {
        try {
            WriteState writeState = beginCycle(announced.advance(versionMinter.mint()));
            task.populate(writeState);
            if(writeEngine.hasChangedSinceLastCycle()) {
                publish(writeState);

                integrityCheck();

                HollowReadStateEngine foo = null;

                // TODO: timt: provide a no-op Validator implementation for now
                validate();

                announced = announce(writeState);
            } else {
                // TODO: timt: replace with listener notification
                System.out.println("BALLOONS!");
                writeEngine.resetToLastPrepareForNextCycle();
            }

        } catch(Throwable th) {
            th.printStackTrace();
            rollback();
        }
    }

    private WriteState beginCycle(HollowStateTransition transition) {
        writeEngine.prepareForNextCycle();
        WriteState writeState = new WriteStateImpl(objectMapper, transition);
        System.out.format("PRODUCING %s\n", transition);
        return writeState;
    }

    private void publish(WriteState writeState) throws IOException {
        HollowBlobWriter writer = new HollowBlobWriter(writeEngine);
        HollowStateTransition transition = writeState.getTransition();

        HollowBlob snapshot = publisher.openSnapshot(transition);
        try {
            writer.writeSnapshot(snapshot.getOutputStream());

            if(transition.isDelta()) {
                HollowBlob delta = publisher.openDelta(transition);
                try {
                    writer.writeDelta(delta.getOutputStream());
                    publisher.publish(delta);
                } finally {
                    delta.close();
                }
                HollowBlob reverseDelta = publisher.openReverseDelta(transition);
                try {
                    writer.writeReverseDelta(reverseDelta.getOutputStream());
                    publisher.publish(reverseDelta);
                } finally {
                    reverseDelta.close();
                }
            }

            /// it's ok to fail to publish a snapshot, as long as you don't miss too many in a row.
            /// you can add a timeout or even do this in a separate thread.
            try {
                publisher.publish(snapshot);
            } catch(Throwable ignored) {
                ignored.printStackTrace(); // TODO: timt: log and notify listerners
            }
        } finally {
            snapshot.close();
        }
    }

    private void integrityCheck() {
        /// Given
        ///
        /// 1. read state (S1) at the previous announced version
        /// 2. read state (S2) from the currently produced snapshot
        ///
        /// Ensure:
        ///
        /// S1.apply(forward delta).checksum == S2.checksum
        /// S2.apply(reverse delta).checksum == S1.checksum
    }

    private void validate() {
        validator.validate(null);
    }

    private HollowStateTransition announce(WriteState writeState) {
        HollowStateTransition transition = writeState.getTransition();
        announcer.announce(transition.getToVersion());
        return transition;
    }

    private void rollback() {
        writeEngine.resetToLastPrepareForNextCycle();
        System.out.format("ROLLED BACK\n");
    }

    public static interface WriteState {
        int add(Object o);

        HollowObjectMapper getObjectMapper();

        HollowWriteStateEngine getStateEngine();

        // TODO: timt: change to getVersion:long
        HollowStateTransition getTransition();
    }

    public static interface VersionMinter {
        /**
         * Create a new state version.<p>
         *
         * State versions should be ascending -- later states have greater versions.<p>
         *
         * @return a new state version
         */
        long mint();
    }

    public static interface Populator {
        void populate(HollowProducer.WriteState newState);
    }

    public static interface Validator {
        static final Validator NO_VALIDATIONS = new Validator(){
            @Override
            public void validate(ReadState readState) {}
        };

        void validate(HollowConsumer.ReadState readState);
    }

}
