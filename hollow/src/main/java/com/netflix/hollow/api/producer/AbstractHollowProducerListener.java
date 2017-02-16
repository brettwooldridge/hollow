package com.netflix.hollow.api.producer;

import java.util.concurrent.TimeUnit;

public class AbstractHollowProducerListener implements HollowProducerListener {
    @Override public void onProducerInit(long elapsed, TimeUnit unit) {}
    @Override public void onProducerRestore(long restoreVersion, long elapsed, TimeUnit unit) {}

    @Override public void onCycleStart(long version) {}
    @Override public void onCycleComplete(ProducerStatus status, long elapsed, TimeUnit unit) {}

    @Override public void onNoDeltaAvailable(long version) {}

    @Override public void onPublishStart(long version) {}
    @Override public void onPublishComplete(ProducerStatus status, long elapsed, TimeUnit unit) {}

    @Override public void onIntegrityCheckStart(long version) {}
    @Override public void onIntegrityCheckComplete(ProducerStatus status, long elapsed, TimeUnit unit) {}

    @Override public void onValidationStart(long version) {}
    @Override public void onValidationComplete(ProducerStatus status, long elapsed, TimeUnit unit) {}

    @Override public void onAnnouncementStart(long version) {}
    @Override public void onAnnouncementComplete(ProducerStatus status, long elapsed, TimeUnit unit) {}
}
