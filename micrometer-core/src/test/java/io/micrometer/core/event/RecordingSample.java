package io.micrometer.core.event;

import java.io.IOException;
import java.util.UUID;

import io.micrometer.core.event.interval.IntervalRecording;
import io.micrometer.core.event.listener.RecordingListener;
import io.micrometer.core.event.listener.composite.AllMatchingCompositeRecordingListener;
import io.micrometer.core.event.listener.composite.CompositeContext;
import io.micrometer.core.instrument.Cardinality;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

class RecordingSample {

    @Test
    void doesItWorkWithRecorder() throws InterruptedException {
        RecordingListener<CompositeContext> listener = new AllMatchingCompositeRecordingListener(
                new SoutRecordingListenerWoContext(),
                new SoutRecordingListenerWContext()
        );
        Recorder recorder = new SimpleRecorder(listener);

        IntervalRecording recording = recorder.recordingFor(() -> "testEvent")
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
        registry.config().recordingListener(listener);

        Timer.Sample sample = Timer.sample(() -> "testEvent", registry)
                .tag(Tag.of("a", "b"))
                .tag(Tag.of("c", UUID.randomUUID().toString(), Cardinality.HIGH))
                .start();

        Thread.sleep(500);
        sample.error(new IOException("simulated"));
        sample.stop();
    }


    static class SoutRecordingListenerWoContext implements RecordingListener<Void> {
        @Override
        public Void createContext() {
            return null;
        }

        @Override
        public void onCreate(IntervalRecording intervalRecording) {
            System.out.println("create: " + intervalRecording + " context: " + intervalRecording.getContext(this));
        }

        @Override
        public void onStart(IntervalRecording intervalRecording) {
            System.out.println("start: " + intervalRecording + " context: " + intervalRecording.getContext(this));

        }

        @Override
        public void onStop(IntervalRecording intervalRecording) {
            System.out.println("stop: " + intervalRecording + " context: " + intervalRecording.getContext(this));
        }

        @Override
        public void onError(IntervalRecording intervalRecording) {
            System.out.println("error: " + intervalRecording + " context: " + intervalRecording.getContext(this));
        }

        @Override
        public void onRestore(IntervalRecording intervalRecording) {
            System.out.println("restore: " + intervalRecording + " context: " + intervalRecording.getContext(this));
        }
    }

    static class SoutRecordingListenerWContext implements RecordingListener<SoutRecordingListenerWContext.TestContext> {
        @Override
        public TestContext createContext() {
            return new TestContext(null);
        }

        @Override
        public void onCreate(IntervalRecording intervalRecording) {
            System.out.println("create: " + intervalRecording + " context: " + intervalRecording.getContext(this));
            intervalRecording.getContext(this).setId("setByCreate");
        }

        @Override
        public void onStart(IntervalRecording intervalRecording) {
            System.out.println("start: " + intervalRecording + " context: " + intervalRecording.getContext(this));
            intervalRecording.getContext(this).setId("setByStart");

        }

        @Override
        public void onStop(IntervalRecording intervalRecording) {
            System.out.println("stop: " + intervalRecording + " context: " + intervalRecording.getContext(this));
            intervalRecording.getContext(this).setId("setByStop");
        }

        @Override
        public void onError(IntervalRecording intervalRecording) {
            System.out.println("error: " + intervalRecording + " context: " + intervalRecording.getContext(this));
            intervalRecording.getContext(this).setId("setByError");
        }

        @Override
        public void onRestore(IntervalRecording intervalRecording) {
            System.out.println("restore: " + intervalRecording + " context: " + intervalRecording.getContext(this));
            intervalRecording.getContext(this).setId("setByRestore");
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
    }

}
