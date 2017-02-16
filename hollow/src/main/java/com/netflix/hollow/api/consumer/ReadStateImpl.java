package com.netflix.hollow.api.consumer;

import com.netflix.hollow.api.client.HollowClient;
import com.netflix.hollow.core.read.engine.HollowReadStateEngine;

// TODO: timt: should be package protected
public class ReadStateImpl implements HollowConsumer.ReadState {
    private final long version;
    private final HollowReadStateEngine stateEngine;

    // TODO: timt: temporary until we stop using HollowClient in HollowProducer
    public ReadStateImpl(HollowClient client) {
        this(client.getCurrentVersionId(), client.getStateEngine());
    }

    // TODO: timt: should be package protected
    public ReadStateImpl(long version, HollowReadStateEngine stateEngine) {
        this.version = version;
        this.stateEngine = stateEngine;
    }


    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public HollowReadStateEngine getStateEngine() {
        return stateEngine;
    }
}
