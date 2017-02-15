package com.netflix.hollow.api.producer;

public class AbstractHollowProducerListener implements HollowProducerListener {
    @Override public void onProducerInit() {}
    @Override public void onProducerRestore(long restoreVersion) {}
    @Override public void onCycleStart(long version) {}
    @Override public void onNoDeltaAvailable(long version) {}
    @Override public void onPublishStart(long version) {}
    @Override public void onPublishComplete(ProducerStatus status) {}
    @Override public void onIntegrityCheckStart(long version) {}
    @Override public void onIntegrityCheckComplete(ProducerStatus status) {}
    @Override public void onValidationStart(long version) {}
    @Override public void onValidationComplete(ProducerStatus status) {}
    @Override public void onAnnouncementStart(long version) {}
    @Override public void onAnnouncementComplete(ProducerStatus status) {}
    @Override public void onCycleComplete(ProducerStatus status) {}
}
