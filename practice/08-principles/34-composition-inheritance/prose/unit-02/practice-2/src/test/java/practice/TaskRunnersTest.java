package practice;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import practice.TaskRunners.LoggingTaskRunner;
import practice.TaskRunners.SynchronizedTaskRunner;
import practice.TaskRunners.TaskRunner;
import practice.TaskRunners.UpperCaseRunner;

import static org.assertj.core.api.Assertions.*;

class TaskRunnersTest {

    /** A base runner whose call for "first" waits inside until released; it records any overlap. */
    static final class Gate implements TaskRunner {
        final CountDownLatch firstInside = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final AtomicInteger active = new AtomicInteger();
        final AtomicBoolean overlapped = new AtomicBoolean();
        final AtomicBoolean secondEntered = new AtomicBoolean();

        @Override
        public String run(String input) throws Exception {
            if (active.incrementAndGet() > 1) {
                overlapped.set(true);
            }
            try {
                if (input.equals("first")) {
                    firstInside.countDown();
                    release.await();
                } else if (input.equals("second")) {
                    secondEntered.set(true);
                }
                return input.toUpperCase();
            } finally {
                active.decrementAndGet();
            }
        }
    }

    /** True once t waits on a java.util.concurrent lock, or is blocked entering a monitor in the practice code. */
    static boolean parkedOnALock(Thread t) {
        Thread.State state = t.getState();
        StackTraceElement[] stack = t.getStackTrace();
        if (state == Thread.State.BLOCKED) {
            return stack.length > 0 && stack[0].getClassName().startsWith("practice.");
        }
        if (state != Thread.State.WAITING && state != Thread.State.TIMED_WAITING) {
            return false;
        }
        for (StackTraceElement e : stack) {
            if (e.getClassName().startsWith("java.util.concurrent.locks.")) {
                return true;
            }
        }
        return false;
    }

    /** Waits (bounded) until the second call either reached the base runner or is waiting on a lock. */
    static void awaitSecond(Thread second, Gate gate) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(60);
        while (!gate.secondEntered.get() && !parkedOnALock(second)) {
            if (System.nanoTime() > deadline) {
                throw new AssertionError("the second call neither ran nor waited");
            }
            Thread.onSpinWait();
        }
    }

    static Thread start(Runnable body) {
        Thread t = new Thread(body);
        t.setDaemon(true);
        t.start();
        return t;
    }

    static void call(TaskRunner runner, String input) {
        try {
            runner.run(input);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void decoratorsWrapAnyRunner() throws Exception {
        List<String> log = new CopyOnWriteArrayList<>();
        TaskRunner base = new UpperCaseRunner();
        TaskRunner runner = TaskRunners.compose(base, log, true, true);
        assertThat(runner.run(new String("hello"))).isEqualTo("HELLO");
        assertThat(log).containsExactly("START: hello", "SUCCESS: HELLO");
        assertThat(new SynchronizedTaskRunner(base).run(new String("abc"))).isEqualTo("ABC");
        assertThat(TaskRunners.compose(base, log, false, false)).isSameAs(base);
        List<String> logOnly = new CopyOnWriteArrayList<>();
        TaskRunner logging = TaskRunners.compose(base, logOnly, true, false);
        assertThat(logging).isInstanceOf(LoggingTaskRunner.class);
        assertThat(logging.run(new String("x"))).isEqualTo("X");
        assertThat(logOnly).containsExactly("START: x", "SUCCESS: X");
        List<String> lockOnly = new CopyOnWriteArrayList<>();
        TaskRunner locked = TaskRunners.compose(base, lockOnly, false, true);
        assertThat(locked).isInstanceOf(SynchronizedTaskRunner.class);
        assertThat(locked.run(new String("y"))).isEqualTo("Y");
        assertThat(lockOnly).as("no logging was asked for").isEmpty();
    }

    @Test
    void loggingRecordsAFailureAndRethrowsIt() throws Exception {
        List<String> log = new CopyOnWriteArrayList<>();
        Exception boom = new IllegalStateException("boom");
        TaskRunner failing = input -> { throw boom; };
        Throwable caught = catchThrowable(() -> new LoggingTaskRunner(failing, log).run("x"));
        assertThat(caught).isSameAs(boom);
        assertThat(log).containsExactly("START: x", "ERROR: boom");
    }

    @Test
    void theSynchronizedRunnerLetsOneCallInAtATime() throws Exception {
        Gate gate = new Gate();
        TaskRunner runner = new SynchronizedTaskRunner(gate);
        assertThat(runner.run("warm-up")).isEqualTo("WARM-UP");
        Thread first = start(() -> call(runner, "first"));
        Thread second = null;
        try {
            assertThat(gate.firstInside.await(60, TimeUnit.SECONDS)).isTrue();
            second = start(() -> call(runner, "second"));
            awaitSecond(second, gate);
            assertThat(gate.overlapped.get()).as("a second call ran while the first was inside").isFalse();
        } finally {
            gate.release.countDown();
        }
        first.join(60_000);
        second.join(60_000);
        assertThat(gate.secondEntered.get()).isTrue();
    }

    @Test
    void composePutsTheLockOutsideTheLog() throws Exception {
        Gate gate = new Gate();
        List<String> log = new CopyOnWriteArrayList<>();
        TaskRunner runner = TaskRunners.compose(gate, log, true, true);
        runner.run("warm-up");
        log.clear();
        Thread first = start(() -> call(runner, "first"));
        Thread second = null;
        try {
            assertThat(gate.firstInside.await(60, TimeUnit.SECONDS)).isTrue();
            second = start(() -> call(runner, "second"));
            awaitSecond(second, gate);
            assertThat(log).as("the waiting call must not log before it holds the lock").containsExactly("START: first");
        } finally {
            gate.release.countDown();
        }
        first.join(60_000);
        second.join(60_000);
        assertThat(log).containsExactly("START: first", "SUCCESS: FIRST", "START: second", "SUCCESS: SECOND");
    }

    @Test
    void eachRunnerHasItsOwnLock() throws Exception {
        java.util.concurrent.CountDownLatch inside = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
        TaskRunners.TaskRunner slow = input -> {
            inside.countDown();
            release.await(10, java.util.concurrent.TimeUnit.SECONDS);
            return input;
        };
        TaskRunners.TaskRunner first = new TaskRunners.SynchronizedTaskRunner(slow);
        TaskRunners.TaskRunner second = new TaskRunners.SynchronizedTaskRunner(new TaskRunners.UpperCaseRunner());
        Thread holder = new Thread(() -> {
            try {
                first.run("a");
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
        holder.setDaemon(true);
        holder.start();
        try {
            assertThat(inside.await(5, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            java.util.concurrent.atomic.AtomicReference<String> other = new java.util.concurrent.atomic.AtomicReference<>();
            Thread caller = new Thread(() -> {
                try {
                    other.set(second.run("b"));
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });
            caller.setDaemon(true);
            caller.start();
            caller.join(3_000);
            assertThat(other.get()).as("the other runner's call was not held up").isEqualTo("B");
        } finally {
            release.countDown();
            holder.join(5_000);
        }
    }
}
