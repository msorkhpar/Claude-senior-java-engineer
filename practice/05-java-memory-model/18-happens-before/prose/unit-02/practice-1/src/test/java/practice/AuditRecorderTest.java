package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class AuditRecorderTest {

    private static final List<String> EVENTS = List.of(
            "login:ana", "view:invoice-17", "export:invoice-17", "logout:ana",
            "login:bo", "delete:draft-3", "login:chen", "view:report-q2",
            "view:report-q3", "logout:bo", "logout:chen");

    /** Runs a call on a daemon thread and waits at most five seconds for it. */
    private static <T> T onAnotherThread(Callable<T> call) throws Exception {
        FutureTask<T> task = new FutureTask<>(call);
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
        return task.get(5, TimeUnit.SECONDS);
    }

    /** An outbox that remembers what each batch held at the moment it was put. */
    static final class RecordingOutbox extends LinkedBlockingQueue<List<String>> {
        final List<List<String>> atHandOff = Collections.synchronizedList(new ArrayList<>());

        @Override
        public void put(List<String> batch) throws InterruptedException {
            atHandOff.add(List.copyOf(batch));
            super.put(batch);
        }

        @Override
        public boolean offer(List<String> batch) {
            atHandOff.add(List.copyOf(batch));
            return super.offer(batch);
        }

        @Override
        public boolean add(List<String> batch) {
            return offer(batch);
        }
    }

    private static Void recordAll(AuditRecorder recorder, List<String> events) throws InterruptedException {
        for (String e : events) recorder.record(e);
        recorder.flush();
        return null;
    }

    @Test
    void handsOverEveryEventOnceInBatchesOfTheConfiguredSize() throws Exception {
        RecordingOutbox outbox = new RecordingOutbox();
        AuditRecorder recorder = new AuditRecorder(outbox, 4);

        onAnotherThread(() -> recordAll(recorder, EVENTS));

        assertThat(outbox.atHandOff).containsExactly(
                List.of("login:ana", "view:invoice-17", "export:invoice-17", "logout:ana"),
                List.of("login:bo", "delete:draft-3", "login:chen", "view:report-q2"),
                List.of("view:report-q3", "logout:bo", "logout:chen"));
    }

    @Test
    void aBatchTheSenderTakesHoldsWhatWasHandedOver() throws Exception {
        BlockingQueue<List<String>> outbox = new LinkedBlockingQueue<>();
        AuditRecorder recorder = new AuditRecorder(outbox, 3);
        List<String> events = EVENTS.subList(0, 8);

        onAnotherThread(() -> recordAll(recorder, events));   // the sender starts once recording is done
        List<List<String>> shipped = onAnotherThread(() -> {
            List<List<String>> taken = new ArrayList<>();
            for (int i = 0; i < 3; i++) taken.add(List.copyOf(outbox.take()));
            return taken;
        });

        assertThat(shipped).containsExactly(
                List.of("login:ana", "view:invoice-17", "export:invoice-17"),
                List.of("logout:ana", "login:bo", "delete:draft-3"),
                List.of("login:chen", "view:report-q2"));
    }
}
