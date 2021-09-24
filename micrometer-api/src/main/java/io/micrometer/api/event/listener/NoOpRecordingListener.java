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

package io.micrometer.api.event.listener;

import io.micrometer.api.event.instant.InstantRecording;
import io.micrometer.api.event.interval.IntervalRecording;

/**
 * Listener that does nothing.
 *
 * @author Marcin Grzejszczak
 * @since 6.0.0
 * @param <T> context type
 */
public class NoOpRecordingListener implements RecordingListener<Object> {

    @Override
    public Object createContext() {
        return null;
    }

    @Override
    public void onCreate(IntervalRecording<Object> intervalRecording) {
    }

    @Override
    public void onStart(IntervalRecording<Object> intervalRecording) {
    }

    @Override
    public void onStop(IntervalRecording<Object> intervalRecording) {
    }

    @Override
    public void onError(IntervalRecording<Object> intervalRecording) {
    }

    @Override
    public void onRestore(IntervalRecording<Object> intervalRecording) {
    }

    @Override
    public void recordInstant(InstantRecording instantRecording) {
    }

}
