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

package io.micrometer.core.event.listener.composite;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.micrometer.api.event.Recording;
import io.micrometer.api.event.instant.InstantRecording;
import io.micrometer.api.event.interval.IntervalRecording;
import io.micrometer.api.event.listener.RecordingListener;
import io.micrometer.api.event.listener.composite.CompositeContext;
import io.micrometer.api.event.listener.composite.CompositeRecordingListener;

/**
 * Using this {@link RecordingListener} implementation, you can register
 * multiple listeners but only the first matching one will be applied.
 *
 * @author Marcin Grzejszczak
 * @since 6.0.0
 */
public class FirstMatchingCompositeRecordingListener implements CompositeRecordingListener {

    private final List<? extends RecordingListener<?>> listeners;

    /**
     * Creates a new instance of {@link FirstMatchingCompositeRecordingListener}.
     *
     * @param listeners the listeners that are registered under the composite
     */
    public FirstMatchingCompositeRecordingListener(RecordingListener<?>... listeners) {
        this(Arrays.asList(listeners));
    }

    /**
     * Creates a new instance of {@link FirstMatchingCompositeRecordingListener}.
     *
     * @param listeners the listeners that are registered under the composite
     */
    public FirstMatchingCompositeRecordingListener(List<? extends RecordingListener<?>> listeners) {
        this.listeners = listeners;
    }

    @Override
    public void onCreate(IntervalRecording intervalRecording) {
        getFirstApplicableListener(intervalRecording)
                .ifPresent(listener -> listener.onCreate(intervalRecording));
    }

    @Override
    public void onStart(IntervalRecording intervalRecording) {
        getFirstApplicableListener(intervalRecording)
                .ifPresent(listener -> listener.onStart(intervalRecording));
    }

    @Override
    public void onStop(IntervalRecording intervalRecording) {
        getFirstApplicableListener(intervalRecording)
                .ifPresent(listener -> listener.onStop(intervalRecording));
    }

    private Optional<? extends RecordingListener<?>> getFirstApplicableListener(Recording<?, ?> recording) {
        return this.listeners.stream().filter(listener -> listener.isApplicable(recording)).findFirst();
    }

    @Override
    public void onError(IntervalRecording intervalRecording) {
        getFirstApplicableListener(intervalRecording)
                .ifPresent(listener -> listener.onError(intervalRecording));
    }

    @Override
    public void onRestore(IntervalRecording intervalRecording) {
        getFirstApplicableListener(intervalRecording)
                .ifPresent(listener -> listener.onRestore(intervalRecording));
    }

    @Override
    public void recordInstant(InstantRecording instantRecording) {
        getFirstApplicableListener(instantRecording).ifPresent(listener -> listener.recordInstant(instantRecording));
    }

    @Override
    public CompositeContext createContext() {
        return new CompositeContext(this.listeners);
    }

    @Override
    public List<? extends RecordingListener<?>> getListeners() {
        return this.listeners;
    }

}
