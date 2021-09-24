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

import io.micrometer.api.event.interval.IntervalRecording;
import io.micrometer.api.event.listener.RecordingListener;
import io.micrometer.api.instrument.tracing.CurrentTraceContext;
import io.micrometer.api.instrument.tracing.Span;
import io.micrometer.api.instrument.tracing.Tracer;

/**
 * Marker interface for tracing listeners.
 *
 * @author Marcin Grzejszczak
 * @since 6.0.0
 */
public interface TracingRecordingListener extends RecordingListener<TracingRecordingListener.TracingContext> {

	@Override
    default void onCreate(IntervalRecording<TracingRecordingListener.TracingContext> intervalRecording) {
		Span span = getTracer().currentSpan();
		if (span != null) {
			setSpanAndScope(intervalRecording, span);
		}
	}

	/**
	 * Sets span and a scope for that span in context.
	 *
	 * @param intervalRecording recording with context to mutate
	 * @param span span to put in context
	 */
    default void setSpanAndScope(IntervalRecording<TracingRecordingListener.TracingContext> intervalRecording,
            Span span) {
		if (span == null) {
			return;
		}
		CurrentTraceContext.Scope scope = getTracer().currentTraceContext().maybeScope(span.context());
        intervalRecording.getContext().setSpanAndScope(span, scope);
	}

	/**
	 * Cleans the scope present in the context.
	 *
	 * @param intervalRecording recording with context containing scope
	 */
    default void cleanup(IntervalRecording<TracingRecordingListener.TracingContext> intervalRecording) {
        TracingContext context = intervalRecording.getContext();
		context.getScope().close();
	}

	@Override
	default TracingContext createContext() {
		return new TracingContext();
	}

	@Override
    default void onRestore(IntervalRecording<TracingRecordingListener.TracingContext> intervalRecording) {
        Span span = intervalRecording.getContext().getSpan();
		setSpanAndScope(intervalRecording, span);
	}

	/**
	 * Returns the {@link Tracer}.
	 *
	 * @return tracer
	 */
	Tracer getTracer();

	/**
	 * Basic tracing context.
	 *
	 * @author Marcin Grzejszczak
	 * @since 6.0.0
	 */
	class TracingContext {

		private Span span;

		private CurrentTraceContext.Scope scope;

		/**
		 * Returns the span.
		 *
		 * @return span
		 */
		Span getSpan() {
			return this.span;
		}

		/**
		 * Sets the span.
		 *
		 * @param span span to set
		 */
		void setSpan(Span span) {
			this.span = span;
		}

		/**
		 * Returns the scope of the span.
		 *
		 * @return scope of the span
		 */
		CurrentTraceContext.Scope getScope() {
			return this.scope;
		}

		/**
		 * Sets the current trace context scope.
		 *
		 * @param scope scope to set
		 */
		void setScope(CurrentTraceContext.Scope scope) {
			this.scope = scope;
		}

		/**
		 * Convenience method to set both span and scope.
		 *
		 * @param span span to set
		 * @param scope scope to set
		 */
		void setSpanAndScope(Span span, CurrentTraceContext.Scope scope) {
			setSpan(span);
			setScope(scope);
		}

	}

}
