package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 20, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class GateTest {

    private static final ThreadMXBean MX = ManagementFactory.getThreadMXBean();

    private static Thread passer(Gate gate, AtomicBoolean passed) {
        Thread thread = new Thread(() -> {
            try {
                gate.pass();
                passed.set(true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    /**
     * Whether, within 5 s, one JVM snapshot of the thread shows it inside Object.wait called from
     * Gate.pass, and (when {@code state} is not null) in that state. A brief block on a JVM-internal
     * lock elsewhere never matches.
     */
    private static boolean awaitInWait(Thread thread, Thread.State state) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            ThreadInfo info = MX.getThreadInfo(thread.threadId(), Integer.MAX_VALUE);
            if (info != null && (state == null || info.getThreadState() == state) && inGateWait(info)) {
                return true;
            }
            Thread.sleep(1);
        }
        return false;
    }

    private static boolean inGateWait(ThreadInfo info) {
        boolean waiting = false;
        boolean inPass = false;
        for (StackTraceElement frame : info.getStackTrace()) {
            waiting |= frame.getClassName().equals("java.lang.Object") && frame.getMethodName().startsWith("wait");
            inPass |= frame.getClassName().equals("practice.Gate") && frame.getMethodName().equals("pass");
        }
        return waiting && inPass;
    }

    @Test
    void aWaiterPassesOnceTheGateOpens() throws InterruptedException {
        Gate gate = new Gate();
        AtomicBoolean passed = new AtomicBoolean();
        Thread waiter = passer(gate, passed);
        assertThat(awaitInWait(waiter, null)).as("the waiter waits on the gate's monitor").isTrue();
        assertThat(passed.get()).as("the waiter passed a closed gate").isFalse();
        gate.open();
        waiter.join(5_000);
        assertThat(passed.get()).as("the waiter passed once the gate opened").isTrue();
    }

    @Test
    void aClosedGateHoldsAWaiterInWaiting() throws InterruptedException {
        Gate gate = new Gate();
        AtomicBoolean passed = new AtomicBoolean();
        Thread waiter = passer(gate, passed);
        boolean seen = awaitInWait(waiter, Thread.State.WAITING);
        gate.open();
        waiter.join(5_000);
        assertThat(seen).as("WAITING inside Object.wait, neither spinning nor sleeping").isTrue();
    }

    @Test
    void aWakeupWithoutOpenDoesNotLetItThrough() throws InterruptedException {
        Gate gate = new Gate();
        AtomicBoolean passed = new AtomicBoolean();
        Thread waiter = passer(gate, passed);
        assertThat(awaitInWait(waiter, null)).as("the waiter waits on the gate's monitor").isTrue();
        long waitsBefore = MX.getThreadInfo(waiter.threadId()).getWaitedCount();
        synchronized (gate) {
            gate.notifyAll();
        }
        // The verdict is taken while the gate is still closed: the woken waiter either waits in pass()
        // again (its count of waits has risen) or has got through.
        boolean waitsAgain = false;
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!waitsAgain && !passed.get() && System.nanoTime() < deadline) {
            ThreadInfo info = MX.getThreadInfo(waiter.threadId(), Integer.MAX_VALUE);
            waitsAgain = info != null && info.getWaitedCount() > waitsBefore
                    && info.getThreadState() == Thread.State.WAITING && inGateWait(info);
            if (!waitsAgain) {
                Thread.sleep(1);
            }
        }
        boolean passedWhileClosed = passed.get();
        gate.open();
        waiter.join(5_000);
        assertThat(passedWhileClosed).as("a wakeup let the waiter through a closed gate").isFalse();
        assertThat(waitsAgain).as("the woken waiter went back to waiting").isTrue();
        assertThat(passed.get()).isTrue();
    }

    @Test
    void anOpenGateDoesNotWait() throws InterruptedException {
        Gate gate = new Gate();
        gate.open();
        AtomicBoolean passed = new AtomicBoolean();
        Thread waiter = passer(gate, passed);
        waiter.join(5_000);
        assertThat(passed.get()).as("pass() on an open gate returned").isTrue();
    }

    @Test
    void openWakesEveryWaiter() throws InterruptedException {
        Gate gate = new Gate();
        AtomicBoolean firstPassed = new AtomicBoolean();
        AtomicBoolean secondPassed = new AtomicBoolean();
        Thread first = passer(gate, firstPassed);
        Thread second = passer(gate, secondPassed);
        assertThat(awaitInWait(first, Thread.State.WAITING)).as("the first thread waits").isTrue();
        assertThat(awaitInWait(second, Thread.State.WAITING)).as("the second thread waits").isTrue();
        gate.open();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        first.join(Math.max(1, TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime())));
        second.join(Math.max(1, TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime())));
        assertThat(firstPassed.get() && secondPassed.get()).as("open() let both waiting threads through").isTrue();
    }

    @Test
    void waitsOnTheGateItself() throws InterruptedException {
        Gate gate = new Gate();
        AtomicBoolean passed = new AtomicBoolean();
        Thread waiter = passer(gate, passed);
        boolean onTheGate = false;
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!onTheGate && System.nanoTime() < deadline) {
            ThreadInfo info = MX.getThreadInfo(waiter.threadId(), Integer.MAX_VALUE);
            onTheGate = info != null && inGateWait(info) && info.getThreadState() == Thread.State.WAITING
                    && info.getLockInfo() != null
                    && info.getLockInfo().getClassName().equals(Gate.class.getName())
                    && info.getLockInfo().getIdentityHashCode() == System.identityHashCode(gate);
            if (!onTheGate) {
                Thread.sleep(1);
            }
        }
        gate.open();
        waiter.join(5_000);
        assertThat(onTheGate).as("the waiter waits on the Gate object's own monitor").isTrue();
    }
}
