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

package io.micrometer.api.event.instant;

import io.micrometer.api.event.Recording;
import io.micrometer.api.event.listener.RecordingListener;

/**
 * Represents the recording of an {@link InstantEvent}. Calling the
 * {@link InstantRecording#record()} method should result in a call of
 * {@link RecordingListener#record(InstantRecording)}.
 *
 * @author Jonatan Ivanov
 * @since 6.0.0
 */
public interface InstantRecording extends Recording<InstantEvent, InstantRecording> {

    /**
     * Signals that an {@link InstantEvent} happened.
     */
    void recordInstant();

    /**
     * Signals that an {@link InstantEvent} happened at a given time.
     *
     * @param wallTime the wall time (system time) in nanoseconds since the epoch at
     *                 the time the event happened
     */
    void recordInstant(long wallTime);

    /**
     * The wall time (system time) in nanoseconds since the epoch at the time the
     * event happened.
     *
     * @return the wall time (system time)
     */
    long getWallTime();

}
