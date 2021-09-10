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

package io.micrometer.core.instrument.listener.tracing;

import java.util.concurrent.TimeUnit;

import io.micrometer.core.event.interval.IntervalRecording;
import io.micrometer.core.event.listener.RecordingListener;
import io.micrometer.core.instrument.tracing.Span;
import io.micrometer.core.instrument.tracing.Tracer;

/**
 * {@link RecordingListener} that uses the Tracing API to record events.
 *
 * @author Marcin Grzejszczak
 * @since 6.0.0
 */
public class DefaultTracingRecordingListener implements TracingRecordingListener {

	private final Tracer tracer;

	private final TracingInstantRecorder tracingInstantRecorder;

	private final TracingTagFilter tracingTagFilter = new TracingTagFilter();

	/**
	 * Creates a new instance of {@link DefaultTracingRecordingListener}.
	 *
	 * @param tracer the tracer to use to record events
	 */
	public DefaultTracingRecordingListener(Tracer tracer) {
		this.tracer = tracer;
		this.tracingInstantRecorder = new TracingInstantRecorder(tracer);
	}

	@Override
    public void onCreate(IntervalRecording intervalRecording) {
		Span span = getTracer().currentSpan();
        intervalRecording.getContext(this).setSpanAndScope(span, () -> {
        });
	}

	@Override
    public void onStart(IntervalRecording intervalRecording) {
        Span parentSpan = intervalRecording.getContext(this).getSpan();
		Span childSpan = parentSpan != null ? getTracer().nextSpan(parentSpan) : getTracer().nextSpan();
		childSpan.name(intervalRecording.getHighCardinalityName())
				.start(getStartTimeInMicros(intervalRecording));
		setSpanAndScope(intervalRecording, childSpan);
	}

	@Override
    public void onStop(IntervalRecording intervalRecording) {
        Span span = intervalRecording.getContext(this).getSpan().name(intervalRecording.getHighCardinalityName());
		this.tracingTagFilter.tagSpan(span, intervalRecording.getTags());
		cleanup(intervalRecording);
		span.end(getStopTimeInMicros(intervalRecording));
	}

	@Override
    public void onError(IntervalRecording intervalRecording) {
        Span span = intervalRecording.getContext(this).getSpan();
		span.error(intervalRecording.getError());
	}

//	@Override
//	public void record(InstantRecording instantRecording) {
//		this.tracingInstantRecorder.record(instantRecording);
//	}

    long getStartTimeInMicros(IntervalRecording recording) {
		return TimeUnit.NANOSECONDS.toMicros(recording.getStartWallTime());
	}

    long getStopTimeInMicros(IntervalRecording recording) {
		return TimeUnit.NANOSECONDS.toMicros(recording.getStartWallTime() + recording.getDuration().toNanos());
	}

	@Override
	public Tracer getTracer() {
		return this.tracer;
	}

}
