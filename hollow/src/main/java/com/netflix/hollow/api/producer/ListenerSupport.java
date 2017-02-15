package com.netflix.hollow.api.producer;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

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

    void fireProducerInit() {
        for(final HollowProducerListener l : listeners) l.onProducerInit();
    }

    void fireProducerRestore(long version) {
        for(final HollowProducerListener l : listeners) l.onProducerRestore(version);
    }

    void fireCycleStart(long version) {
        for(final HollowProducerListener l : listeners) l.onCycleStart(version);
    }

    void fireCycleComplete(ProducerStatus cycleStatus) {
        for(final HollowProducerListener l : listeners) l.onCycleComplete(cycleStatus);
    }

    void fireNoDelta(long version) {
        for(final HollowProducerListener l : listeners) l.onNoDeltaAvailable(version);
    }

    void firePublishStart(long version) {
        for(final HollowProducerListener l : listeners) l.onPublishStart(version);
    }

    void firePublishComplete(ProducerStatus publishStatus) {
        for(final HollowProducerListener l : listeners) l.onPublishComplete(publishStatus);
    }

    void fireIntegrityCheckStart(long version) {
        for(final HollowProducerListener l : listeners) l.onIntegrityCheckStart(version);
    }

    void fireIntegrityCheckComplete(ProducerStatus integrityCheckStatus) {
        for(final HollowProducerListener l : listeners) l.onIntegrityCheckComplete(integrityCheckStatus);
    }

    void fireValidationStart(long version) {
        for(final HollowProducerListener l : listeners) l.onValidationStart(version);
    }

    void fireValidationComplete(ProducerStatus validationStatus) {
        for(final HollowProducerListener l : listeners) l.onValidationComplete(validationStatus);
    }

    void fireAnnouncementStart(long version) {
        for(final HollowProducerListener l : listeners) l.onAnnouncementStart(version);
    }

    void fireAnnouncementComplete(ProducerStatus announcementStatus) {
        for(final HollowProducerListener l : listeners) l.onAnnouncementComplete(announcementStatus);
    }
}
