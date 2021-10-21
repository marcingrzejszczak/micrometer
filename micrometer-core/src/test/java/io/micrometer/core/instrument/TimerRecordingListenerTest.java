/**
 * Copyright 2021 VMware, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micrometer.core.instrument;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.handler.MutableSpan;
import brave.test.TestSpanHandler;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

class TimerRecordingListenerTest {
    MeterRegistry meterRegistry = new SimpleMeterRegistry();
    TestSpanHandler spans = new TestSpanHandler();
    Tracing tracing = Tracing.newBuilder().localServiceName(this.getClass().getSimpleName()).addSpanHandler(spans).build();

    @BeforeEach
    void setup() {
        meterRegistry.config().timerRecordingListener(new BraveTimerRecordingListener(tracing.tracer()));
    }

    @Test
    void timerAndSpansProduced() throws InterruptedException {
        Timer.Sample sample = Timer.start(meterRegistry);
        pause();
        sample.stop(Timer.builder("payment.processing").tags("method", "credit card").register(meterRegistry));

        // timer
        Timer actual = meterRegistry.get("payment.processing").timer();
        assertThat(actual.getId().getTags()).containsExactly(Tag.of("method", "credit card"));
        assertThat(actual.count()).isOne();
        assertThat(actual.totalTime(TimeUnit.NANOSECONDS)).isPositive();

        // span
        MutableSpan span = spans.get(0);
        assertThat(span.name()).isEqualTo("payment.processing");
        assertThat(span.tags()).containsOnlyKeys("method").containsValue("credit card");
        assertThat(span.finishTimestamp()).isNotZero();
    }

    @Test
    void timeRunnable() throws InterruptedException {
        Timer.Sample sample = Timer.start(meterRegistry);
        Span initialSpan = tracing.tracer().currentSpan();
        Runnable myRunnable = () -> {
            String traceIdInsideRunnable = tracing.tracer().currentSpan().context().traceIdString();
            String spanIdInsideRunnable = tracing.tracer().currentSpan().context().spanIdString();
            String parentSpanIdInsideRunnable = tracing.tracer().currentSpan().context().parentIdString();

            assertThat(traceIdInsideRunnable).isEqualTo(initialSpan.context().traceIdString());
            assertThat(spanIdInsideRunnable).isNotEqualTo(initialSpan.context().spanIdString());
            assertThat(parentSpanIdInsideRunnable).isEqualTo(initialSpan.context().spanIdString());
        };
        ExecutorService executor = ExecutorServiceMetrics.monitor(meterRegistry, Executors.newCachedThreadPool(), "my.executor");
        executor.submit(myRunnable);
        executor.shutdown();
        assertThat(executor.awaitTermination(200, TimeUnit.MILLISECONDS)).isTrue();
        sample.stop(Timer.builder("test.timer").register(meterRegistry));

        assertThat(spans.spans()).extracting("name").containsExactly("executor.idle", "executor", "test.timer");
        assertThat(spans.spans()).extracting("traceId").containsOnly(initialSpan.context().traceIdString());
    }

    void pause() throws InterruptedException {
        Thread.sleep(new Random().nextInt(2000));
    }

    static class BraveTimerRecordingListener implements TimerRecordingListener {
        final Tracer tracer;
        // TODO not specific to this listener but leaks are possible where onStart is called but onStop is not
        ConcurrentMap<Timer.Sample, SpanContext> contextMap = new ConcurrentHashMap<>();

        BraveTimerRecordingListener(Tracer tracer) {
            this.tracer = tracer;
        }

        @Override
        public void onStart(Timer.Sample sample) {
            // TODO check if onStart has already been called for this sample?
            Span span = tracer.nextSpan().start();
            contextMap.computeIfAbsent(sample, key -> new SpanContext(span, tracer.withSpanInScope(span)));
        }

        @Override
        public void onError(Timer.Sample sample, Throwable throwable) {
            contextMap.get(sample).getSpan().error(throwable);
        }

        @Override
        public void onStop(Timer.Sample sample, Timer timer, Duration duration) {
            // TODO check if onStart was called for this sample and onStop hasn't been called yet?
            SpanContext context = contextMap.get(sample);
            Span span = context.getSpan().name(timer.getId().getName());
            timer.getId().getTagsAsIterable().forEach(tag -> span.tag(tag.getKey(), tag.getValue()));
            context.getSpanInScope().close();
            span.finish();
        }

        static class SpanContext {
            private final Span span;
            private final Tracer.SpanInScope spanInScope;

            SpanContext(Span span, Tracer.SpanInScope spanInScope) {
                this.span = span;
                this.spanInScope = spanInScope;
            }

            Span getSpan() {
                return span;
            }

            Tracer.SpanInScope getSpanInScope() {
                return spanInScope;
            }
        }
    }
}
