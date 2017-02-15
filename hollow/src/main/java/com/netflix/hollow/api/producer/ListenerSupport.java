package com.netflix.hollow.api.producer;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.netflix.hollow.api.HollowStateTransition;
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

    void fireProducerRestore(HollowStateTransition transition) {
        for(final HollowProducerListener l : listeners) l.onProducerRestore(transition.getToVersion());
    }

    void fireCycleStart(HollowStateTransition transition) {
        for(final HollowProducerListener l : listeners) l.onCycleStart(transition.getToVersion());
    }

    void fireCycleComplete(ProducerStatus cycleStatus) {
        for(final HollowProducerListener l : listeners) l.onCycleComplete(cycleStatus);
    }

    void fireNoDelta(HollowStateTransition transition) {
        for(final HollowProducerListener l : listeners) l.onNoDeltaAvailable(transition.getToVersion());
    }

    void firePublishStart(HollowStateTransition transition) {
        for(final HollowProducerListener l : listeners) l.onPublishStart(transition.getToVersion());
    }

    void firePublishComplete(ProducerStatus publishStatus) {
        for(final HollowProducerListener l : listeners) l.onPublishComplete(publishStatus);
    }

    void fireIntegrityCheckStart(HollowStateTransition transition) {
        for(final HollowProducerListener l : listeners) l.onIntegrityCheckStart(transition.getToVersion());
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

    void fireAnnouncementStart(HollowStateTransition transition) {
        for(final HollowProducerListener l : listeners) l.onAnnouncementStart(transition.getToVersion());
    }

    void fireAnnouncementComplete(ProducerStatus announcementStatus) {
        for(final HollowProducerListener l : listeners) l.onAnnouncementComplete(announcementStatus);
    }
}
