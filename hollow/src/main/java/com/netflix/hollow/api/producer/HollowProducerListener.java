package com.netflix.hollow.api.producer;

import java.util.EventListener;

/**
 * A class should implement {@code HollowProducerListener}, if it wants to be notified on start/completion of various stages of the {@link HollowProducer}.
 *
 * @author Kinesh Satiya {@literal kineshsatiya@gmail.com}.
 */
public interface HollowProducerListener extends EventListener {

    /**
     * Called after the {@code HollowProducer} has initialized its data model.
     *
     * @param producer the producer which initialized
     */
    public void onProducerInit(HollowProducer producer);

    /**
     * Called after the {@code HollowProducer} has restored its data state to the indicated version.
     * If previous state is not available to restore from, then this callback will not be called.
     *
     * @param producer the producer which restored
     * @param restoreVersion Version from which the state for {@code HollowProducer} was restored.
     */
    public void onProducerRestore(HollowProducer producer, long restoreVersion);

    /**
     * Called when the {@code HollowProducer} has begun a new cycle.
     *
     * @param producer the producer which started a cycle
     * @param version Version produced by the {@code HollowProducer} for new cycle about to start.
     */
    public void onCycleStart(HollowProducer producer, long version);

    /**
     * Called after the new state has been populated if the {@code HollowProducer} detects that no data has changed, thus no snapshot nor delta should be produced.<p>
     *
     * This is a terminal cycle stage; no other stages notifications will be sent for this cycle; the {@link #onCycleComplete(CycleStatus)} will be
     * notified with @{code SUCCESS}.
     *
     * @param producer the producer
     * @param version Current version of the cycle.
     */
    public void onNoDeltaAvailable(HollowProducer producer, long version);

    /**
     * Called when the {@code HollowProducer} has begun publishing the {@code HollowBlob}s produced this cycle.
     *
     * @param producer the producer beginning to publish
     * @param version Version to be published.
     */
    public void onPublishStart(HollowProducer producer, long version);

    /**
     * Called after the publish stage finishes normally or abnormally. A {@code SUCCESS} status indicates that
     * the {@code HollowBlob}s produced this cycle has been published to the blob store.
     *
     * @param status CycleStatus of the publish stage. {@link com.netflix.hollow.api.producer.CycleStatus#getStatus()} will return {@code SUCCESS}
     *   when the publish was successful; @{code FAIL} otherwise.
     */
    public void onPublishComplete(CycleStatus status);

    /**
     * Called when the {@code HollowProducer} has begun checking the integrity of the {@code HollowBlob}s produced this cycle.
     *
     * @param producer the producer beginning the integrity checks
     * @param version Version to be checked
     */
    public void onIntegrityCheckStart(HollowProducer producer, long version);

    /**
     * Called after the integrity check stage finishes normally or abnormally. A {@code SUCCESS} status indicates that
     * the previous snapshot, current snapshot, delta, and reverse-delta {@code HollowBlob}s are all internally consistent.
     *
     * @param status CycleStatus of the integrity check stage. {@link com.netflix.hollow.api.producer.CycleStatus#getStatus()} will return {@code SUCCESS}
     *   when the blobs are internally consistent; @{code FAIL} otherwise.
     */
    public void onIntegrityCheckComplete(CycleStatus status);

    /**
     * Called when the {@code HollowProducer} has begun validating the new data state produced this cycle.
     *
     * @param producer the producer beginning the validation checks
     * @param version Version to be validated
     */
    public void onValidationStart(HollowProducer producer, long version);

    /**
     * Called after the validation stage finishes normally or abnormally. A {@code SUCCESS} status indicates that
     * the newly published data state is considered valid.
     *
     * @param status CycleStatus of the publish stage. {@link com.netflix.hollow.api.producer.CycleStatus#getStatus()} will return {@code SUCCESS}
     *   when the publish was successful; @{code FAIL} otherwise.
     */
    public void onValidationComplete(CycleStatus status);

    /**
     * Called when the {@code HollowProducer} has begun announcing the {@code HollowBlob} published this cycle.
     *
     * @param producer the proucer beginning the announcement
     * @param version of {@code HollowBlob} that will be announced.
     */
    public void onAnnouncementStart(HollowProducer producer, long version);

    /**
     * Called after the announcement stage finishes normally or abnormally. A {@code SUCCESS} status indicates
     * that the {@code HollowBlob} published this cycle has been announced to consumers.
     *
     * @param status CycleStatus of the announcement stage. {@link com.netflix.hollow.api.producer.CycleStatus#getStatus()} will return {@code SUCCESS}
     *   when the announce was successful; @{code FAIL} otherwise.
     */
    public void onAnnouncementComplete(CycleStatus status);

    /**
     * Called after {@code HollowProducer} has completed a cycle normally or abnormally. A {@code SUCCESS} status indicates that the
     * entire cycle was successful and the producer is available to begin another cycle.
     *
     * @param status CycleStatus of this cycle. {@link com.netflix.hollow.api.producer.CycleStatus#getStatus()} will return {@code SUCCESS}
     *   when the a new data state has been announced to consumers or when there were no changes to the data; it will return @{code FAIL}
     *   when any stage failed or any other failure occured during the cycle.
     */
    public void onCycleComplete(CycleStatus status);

}