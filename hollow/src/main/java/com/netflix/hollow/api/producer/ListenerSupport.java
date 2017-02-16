package com.netflix.hollow.api.producer;

import static java.lang.System.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.netflix.hollow.api.consumer.HollowConsumer;
import com.netflix.hollow.api.consumer.HollowConsumer.ReadState;
import com.netflix.hollow.api.producer.HollowProducerListener.ProducerStatus;

final class ListenerSupport {
    private final Set<HollowProducerListener> listeners;

    ListenerSupport() {
        listeners = new CopyOnWriteArraySet<>();
    }

    void add(HollowProducerListener listener) {
        listeners.add(listener);
    }

    void remove(HollowProducerListener listener) {
        listeners.remove(listener);
    }

    void fireProducerInit(long elapsedMillis) {
        for(final HollowProducerListener l : listeners) l.onProducerInit(elapsedMillis, MILLISECONDS);
    }

    void fireProducerRestore(long version, long elapsedMillis) {
        for(final HollowProducerListener l : listeners) l.onProducerRestore(version, elapsedMillis, MILLISECONDS);
    }

    long fireCycleStart(long version) {
        long start = currentTimeMillis();
        for(final HollowProducerListener l : listeners) l.onCycleStart(version);
        return start;
    }

    void fireCycleComplete(ProducerStatus cycleStatus, long startMillis) {
        long elapsedMillis = currentTimeMillis() - startMillis;
        for(final HollowProducerListener l : listeners) l.onCycleComplete(cycleStatus, elapsedMillis, MILLISECONDS);
    }

    void fireNoDelta(long version) {
        for(final HollowProducerListener l : listeners) l.onNoDeltaAvailable(version);
    }

    long firePublishStart(long version) {
        long start = currentTimeMillis();
        for(final HollowProducerListener l : listeners) l.onPublishStart(version);
        return start;
    }

    void firePublishComplete(ProducerStatus publishStatus, long startMillis) {
        long elapsedMillis = currentTimeMillis() - startMillis;
        for(final HollowProducerListener l : listeners) l.onPublishComplete(publishStatus, elapsedMillis, MILLISECONDS);
    }

    long fireIntegrityCheckStart(HollowProducer.WriteState writeState) {
        long start = currentTimeMillis();
        for(final HollowProducerListener l : listeners) l.onIntegrityCheckStart(writeState.getVersion());
        return start;
    }

    void fireIntegrityCheckComplete(ProducerStatus integrityCheckStatus, long startMillis) {
        long elapsedMillis = currentTimeMillis() - startMillis;
        for(final HollowProducerListener l : listeners) l.onIntegrityCheckComplete(integrityCheckStatus, elapsedMillis, MILLISECONDS);
    }

    long fireValidationStart(HollowConsumer.ReadState readState) {
        long start = currentTimeMillis();
        for(final HollowProducerListener l : listeners) l.onValidationStart(readState.getVersion());
        return start;
    }

    void fireValidationComplete(ProducerStatus validationStatus, long startMillis) {
        long elapsedMillis = currentTimeMillis() - startMillis;
        for(final HollowProducerListener l : listeners) l.onValidationComplete(validationStatus, elapsedMillis, MILLISECONDS);
    }

    long fireAnnouncementStart(HollowConsumer.ReadState readState) {
        long start = currentTimeMillis();
        for(final HollowProducerListener l : listeners) l.onAnnouncementStart(readState.getVersion());
        return start;
    }

    void fireAnnouncementComplete(ProducerStatus announcementStatus, long startMillis) {
        long elapsedMillis = currentTimeMillis() - startMillis;
        for(final HollowProducerListener l : listeners) l.onAnnouncementComplete(announcementStatus, elapsedMillis, MILLISECONDS);
    }
}
