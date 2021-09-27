package io.micrometer.api.instrument;

import java.time.Duration;
import java.util.Objects;

import io.micrometer.api.event.Recorder;
import io.micrometer.api.event.instant.InstantEvent;
import io.micrometer.api.event.interval.IntervalEvent;
import io.micrometer.api.event.interval.IntervalRecording;
import io.micrometer.api.instrument.util.StringUtils;
import io.micrometer.api.lang.NonNull;
import io.micrometer.api.lang.Nullable;

/**
 * TODO: Maybe this should go away at all! Just use IntervalRecording instead?
 * TODO: Add a recordInstant on IntervalRecording
 */
public class Sample implements IntervalRecording<Sample>, AutoCloseable {

    /**
     * Creates the sample.
     * 
     * @param event    interval event
     * @param recorder recorder
     * @return created sample
     */
    public static Sample sample(IntervalEvent event, @NonNull Recorder<?> recorder) {
        Objects.requireNonNull(recorder, "Recorder must not be null");
        return new Sample(recorder, event);
    }

    /**
     * Creates the sample.
     * 
     * @param lowCardinalityName low cardinality name
     * @param recorder           recorder
     * @return created sample
     */
    public static Sample sample(String lowCardinalityName, @NonNull Recorder<?> recorder) {
        Objects.requireNonNull(recorder, "Recorder must not be null");
        return new Sample(recorder, () -> lowCardinalityName);
    }

    /**
     * Creates the sample with a default event.
     * 
     * @param recorder recorder
     * @return created sample
     */
    public static Sample sample(@NonNull Recorder<?> recorder) {
        return sample(() -> "sample", recorder);
    }

    /**
     * Creates and starts the sample.
     * 
     * @param event    event
     * @param recorder recorder
     * @return started sample
     */
    public static Sample start(IntervalEvent event, @NonNull Recorder<?> recorder) {
        return sample(event, recorder).start();
    }

    /**
     * Creates and starts the sample.
     * 
     * @param lowCardinalityName low cardinality name
     * @param recorder           recorder
     * @return started sample
     */
    public static Sample start(String lowCardinalityName, @NonNull Recorder<?> recorder) {
        return sample(() -> lowCardinalityName, recorder).start();
    }

    /**
     * Creates and starts the sample with the default event.
     * 
     * @param recorder recorder
     * @return started sample
     */
    public static Sample start(@NonNull Recorder<?> recorder) {
        return sample(recorder).start();
    }

    private final Recorder<?> recorder;

    private final IntervalRecording<?> recording;

    private String description;

    private String lowCardinalityName;

    Sample(Recorder<?> recorder, IntervalEvent event) {
        this.recorder = recorder;
        this.recording = recorder.recordingFor(event);
    }

    @Override
    public IntervalEvent getEvent() {
        return recording.getEvent();
    }

    @Override
    public String getHighCardinalityName() {
        return recording.getHighCardinalityName();
    }

    @Override
    public Sample setHighCardinalityName(String highCardinalityName) {
        recording.setHighCardinalityName(highCardinalityName);
        return this;
    }

    @Override
    public Duration getDuration() {
        return recording.getDuration();
    }

    @Override
    public long getStartNanos() {
        return recording.getStartNanos();
    }

    @Override
    public Sample start() {
        recording.start();
        return this;
    }

    @Override
    public Sample start(long wallTime, long monotonicTime) {
        recording.start(wallTime, monotonicTime);
        return this;
    }

    @Override
    public long getStopNanos() {
        return recording.getStopNanos();
    }

    @Override
    public long getStartWallTime() {
        return recording.getStartWallTime();
    }

    @Override
    public void stop() {
        customizeBeforeStop();
        recording.stop();
    }

    @Override
    public void stop(long monotonicTime) {
        customizeBeforeStop();
        recording.stop(monotonicTime);
    }

    @Override
    public Sample restore() {
        recording.restore();
        return this;
    }

    @Override
    public Iterable<Tag> getTags() {
        return recording.getTags();
    }

    @Override
    public Sample tag(Tag tag) {
        recording.tag(tag);
        return this;
    }

    @Override
    @Nullable
    public Throwable getError() {
        return recording.getError();
    }

    @Override
    public Sample error(Throwable error) {
        recording.error(error);
        return this;
    }

    @Override
    public String toString() {
        return recording.toString();
    }

    @Override
    public void close() {
        customizeBeforeStop();
        recording.close();
    }

    private void customizeBeforeStop() {
        if (StringUtils.isNotBlank(description)) {
            getEvent().setDescription(description);
        }
        if (StringUtils.isNotBlank(lowCardinalityName)) {
            getEvent().setLowCardinalityName(lowCardinalityName);
        }
        recorder.getRecordingCustomizers().forEach(rc -> rc.customize(recording));
    }

    @Override
    public Sample getContext() {
        return this;
    }

    public Sample setDescription(String description) {
        this.description = description;
        return this;
    }

    public Sample setLowCardinalityName(String lowCardinalityName) {
        this.lowCardinalityName = lowCardinalityName;
        return this;
    }

    @Override
    public void recordInstant(InstantEvent event) {
        this.recording.recordInstant(event);
    }
}