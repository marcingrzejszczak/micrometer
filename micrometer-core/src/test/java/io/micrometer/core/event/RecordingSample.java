package io.micrometer.core.event;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.micrometer.api.event.Recorder;
import io.micrometer.api.event.SimpleRecorder;
import io.micrometer.api.event.instant.InstantRecording;
import io.micrometer.api.event.interval.IntervalEvent;
import io.micrometer.api.event.interval.IntervalRecording;
import io.micrometer.api.event.listener.RecordingListener;
import io.micrometer.api.event.listener.composite.CompositeContext;
import io.micrometer.api.instrument.Cardinality;
import io.micrometer.api.instrument.Clock;
import io.micrometer.api.instrument.Sample;
import io.micrometer.api.instrument.Tag;
import io.micrometer.core.event.listener.composite.AllMatchingCompositeRecordingListener;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class RecordingSample {

    @Test
    void doesItWorkWithRecorder() throws InterruptedException {
        RecordingListener<CompositeContext> listener = new AllMatchingCompositeRecordingListener(
                new SoutRecordingListenerWoContext(),
                new SoutRecordingListenerWContext()
        );
        Recorder<?> recorder = new SimpleRecorder(listener, Clock.SYSTEM, Collections.emptyList());

        IntervalRecording recording = recorder.recordingFor((IntervalEvent) () -> "testEvent")
                .tag(Tag.of("a", "b"))
                .tag(Tag.of("c", UUID.randomUUID().toString(), Cardinality.HIGH))
                .start();

        Thread.sleep(500);
        recording.error(new IOException("simulated"));
        recording.stop();
    }

    @Test
    void doesItWorkWithTimerSample() throws InterruptedException {
        RecordingListener<CompositeContext> listener = new AllMatchingCompositeRecordingListener(
                new SoutRecordingListenerWoContext(),
                new SoutRecordingListenerWContext()
        );
        MeterRegistry registry = new SimpleMeterRegistry();
        registry.config().recorder(new SimpleRecorder<>(listener, Clock.SYSTEM, Collections.emptyList()));

        Sample sample = Sample.sample(() -> "testEvent", registry.config().recorder())
                .tag(Tag.of("a", "b"))
                .tag(Tag.of("c", UUID.randomUUID().toString(), Cardinality.HIGH))
                .start();

        Thread.sleep(500);
        sample.error(new IOException("simulated"));
        sample.stop();
    }

    @Test
    void doesItWorkWithTimer() throws InterruptedException {
        RecordingListener<CompositeContext> listener = new AllMatchingCompositeRecordingListener(
                new SoutRecordingListenerWoContext(), new SoutRecordingListenerWContext());
        MeterRegistry registry = new SimpleMeterRegistry();
        registry.config().recorder(new SimpleRecorder<>(listener, Clock.SYSTEM, Collections.emptyList()));

        registry.timer("testEvent",
                Collections.singletonList(Tag.of("c", UUID.randomUUID().toString(), Cardinality.HIGH)))
                .record(() -> System.out.println("HELLO"));

        Thread.sleep(500);
    }


    static class SoutRecordingListenerWoContext implements RecordingListener<Void> {
        @Override
        public Void createContext() {
            return null;
        }

        @Override
        public void onCreate(IntervalRecording<Void> intervalRecording) {
            System.out.println("create: " + intervalRecording + " context: " + intervalRecording.getContext());
        }

        @Override
        public void onStart(IntervalRecording<Void> intervalRecording) {
            System.out.println("start: " + intervalRecording + " context: " + intervalRecording.getContext());

        }

        @Override
        public void onStop(IntervalRecording<Void> intervalRecording) {
            System.out.println("stop: " + intervalRecording + " context: " + intervalRecording.getContext());
        }

        @Override
        public void onError(IntervalRecording<Void> intervalRecording) {
            System.out.println("error: " + intervalRecording + " context: " + intervalRecording.getContext());
        }

        @Override
        public void onRestore(IntervalRecording<Void> intervalRecording) {
            System.out.println("restore: " + intervalRecording + " context: " + intervalRecording.getContext());
        }

        @Override
        public void recordInstant(InstantRecording instantRecording) {
        }
    }

    static class SoutRecordingListenerWContext implements RecordingListener<SoutRecordingListenerWContext.TestContext> {
        @Override
        public TestContext createContext() {
            return new TestContext(null);
        }

        @Override
        public void onCreate(IntervalRecording<TestContext> intervalRecording) {
            System.out.println("create: " + intervalRecording + " context: " + intervalRecording.getContext());
            intervalRecording.getContext().setId("setByCreate");
        }

        @Override
        public void onStart(IntervalRecording<TestContext> intervalRecording) {
            System.out.println("start: " + intervalRecording + " context: " + intervalRecording.getContext());
            intervalRecording.getContext().setId("setByStart");

        }

        @Override
        public void onStop(IntervalRecording<TestContext> intervalRecording) {
            System.out.println("stop: " + intervalRecording + " context: " + intervalRecording.getContext());
            intervalRecording.getContext().setId("setByStop");
        }

        @Override
        public void onError(IntervalRecording<TestContext> intervalRecording) {
            System.out.println("error: " + intervalRecording + " context: " + intervalRecording.getContext());
            intervalRecording.getContext().setId("setByError");
        }

        @Override
        public void onRestore(IntervalRecording<TestContext> intervalRecording) {
            System.out.println("restore: " + intervalRecording + " context: " + intervalRecording.getContext());
            intervalRecording.getContext().setId("setByRestore");
        }

        static class TestContext {
            private String id;

            TestContext(String id) {
                this.id = id;
            }

            String getId() {
                return id;
            }

            void setId(String id) {
                this.id = id;
            }

            @Override
            public String toString() {
                return "TestContext{ id=" + id + " }";
            }
        }

        @Override
        public void recordInstant(InstantRecording instantRecording) {

        }
    }

}
