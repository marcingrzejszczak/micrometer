/*
 * Copyright 2021-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.micrometer.core.instrument.listener.metrics;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import io.micrometer.api.event.Recording;
import io.micrometer.api.event.instant.InstantRecording;
import io.micrometer.api.event.interval.IntervalLongRunningHttpServerEvent;
import io.micrometer.api.event.interval.IntervalRecording;
import io.micrometer.api.event.listener.RecordingListener;
import io.micrometer.api.instrument.Cardinality;
import io.micrometer.api.instrument.Tag;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * {@link RecordingListener} that uses Micrometer's API to record long running
 * tasks.
 *
 * @author Marcin Grzejszczak
 * @since 6.0.0
 */
public class MicrometerLongRunningTaskRecordingListener
        implements MetricsRecordingListener<MicrometerLongRunningTaskRecordingListener.LongRunningTaskContext> {

    private final MeterRegistry registry;

    /**
     * Creates a new instance of {@link MicrometerLongRunningTaskRecordingListener}.
     *
     * @param registry the registry to use to record events
     */
    public MicrometerLongRunningTaskRecordingListener(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public boolean isApplicable(Recording<?, ?> recording) {
        return recording.getEvent() instanceof IntervalLongRunningHttpServerEvent;
    }

    @Override
    public void onStart(IntervalRecording intervalRecording) {
        LongTaskTimer.Sample sample = LongTaskTimer.builder(intervalRecording.getEvent().getLowCardinalityName())
                .description(intervalRecording.getEvent().getDescription()).tags(toTags(intervalRecording))
                .register(this.registry).start();
        intervalRecording.getContext(this).addSample(sample);
    }

    @Override
    public void onStop(IntervalRecording intervalRecording) {
        intervalRecording.getContext(this).getSample().stop();
    }

    @Override
    public void onError(IntervalRecording intervalRecording) {
        // TODO: If error add a tag
    }

    @Override
    public void recordInstant(InstantRecording instantRecording) {

    }

    @Override
    public LongRunningTaskContext createContext() {
        return new LongRunningTaskContext();
    }

    // TODO: Duplicated code
    private List<Tag> toTags(Recording<?, ?> recording) {
        return StreamSupport.stream(recording.getTags().spliterator(), false)
                .filter(tag -> tag.getCardinality() == Cardinality.LOW).map(tag -> Tag.of(tag.getKey(), tag.getValue()))
                .collect(Collectors.toList());
    }

    static class LongRunningTaskContext {

        private LongTaskSample sample;

        void addSample(LongTaskSample sample) {
            this.sample = sample;
        }

        LongTaskSample getSample() {
            return this.sample;
        }

    }

}
