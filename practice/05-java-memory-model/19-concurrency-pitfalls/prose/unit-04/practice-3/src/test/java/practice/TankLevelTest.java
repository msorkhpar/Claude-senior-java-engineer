package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class TankLevelTest {

    private final ExecutorService caller = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "sensor");
        t.setDaemon(true);
        return t;
    });

    @AfterEach
    void stop() {
        caller.shutdownNow();
    }

    /** Runs the calls on a daemon thread, so that code that blocks fails the test instead of hanging it. */
    private void onSensorThread(Runnable calls) throws Exception {
        caller.submit(calls).get(5, TimeUnit.SECONDS);
    }

    private static List<Long> heard() {
        return Collections.synchronizedList(new ArrayList<>());
    }

    @Test
    void everySubscriberHearsEachReading() throws Exception {
        TankLevel tank = new TankLevel();
        List<Long> panel = heard();
        List<Long> alarm = heard();
        tank.subscribe(panel::add);
        tank.subscribe(alarm::add);

        onSensorThread(() -> {
            tank.update(12_400);
            tank.update(12_650);
        });

        assertThat(panel).containsExactly(12_400L, 12_650L);
        assertThat(alarm).containsExactly(12_400L, 12_650L);
        assertThat(tank.litres()).isEqualTo(12_650L);
    }

    @Test
    void aReadingWithNoSubscribersIsStillRecorded() throws Exception {
        TankLevel tank = new TankLevel();

        onSensorThread(() -> tank.update(3_075));

        assertThat(tank.litres()).isEqualTo(3_075L);
    }

    @Test
    void aPanelThatReadsTheTankUnderItsOwnLockDoesNotFreezeTheSensor() throws Exception {
        TankLevel tank = new TankLevel();
        Object panelLock = new Object();
        CountDownLatch panelHoldsItsLock = new CountDownLatch(1);
        CountDownLatch readingReachedPanel = new CountDownLatch(1);
        List<Long> shown = heard();
        tank.subscribe(litres -> {                         // the panel's subscriber
            readingReachedPanel.countDown();
            synchronized (panelLock) {
                shown.add(litres);
            }
        });

        Thread panel = new Thread(() -> {                  // the panel redraws under its own lock
            synchronized (panelLock) {
                panelHoldsItsLock.countDown();
                try {
                    readingReachedPanel.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    return;
                }
                tank.litres();
            }
        }, "panel");
        Thread sensor = new Thread(() -> tank.update(18_900), "sensor");
        panel.setDaemon(true);
        sensor.setDaemon(true);
        panel.start();
        assertThat(panelHoldsItsLock.await(5, TimeUnit.SECONDS)).isTrue();
        sensor.start();

        ThreadMXBean mx = ManagementFactory.getThreadMXBean();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        List<String> deadlocked = List.of();
        while ((panel.isAlive() || sensor.isAlive()) && System.nanoTime() < deadline && deadlocked.isEmpty()) {
            panel.join(10);
            long[] ids = mx.findDeadlockedThreads();
            if (ids != null) {
                List<String> cycle = new ArrayList<>();
                for (ThreadInfo info : mx.getThreadInfo(ids)) {
                    if (info != null && (info.getThreadId() == panel.getId() || info.getThreadId() == sensor.getId())) {
                        cycle.add(info.getThreadName() + " waits for " + info.getLockName() + " held by " + info.getLockOwnerName());
                    }
                }
                deadlocked = cycle;
            }
        }

        assertThat(deadlocked).as("threads deadlocked").isEmpty();
        assertThat(panel.isAlive() || sensor.isAlive()).as("panel or sensor still running after 5 s").isFalse();
        assertThat(shown).containsExactly(18_900L);
    }

    @Test
    void aSubscriberThatSubscribesAnotherDuringADeliveryDoesNotBreakIt() throws Exception {
        TankLevel tank = new TankLevel();
        List<Long> pump = heard();
        List<Long> panel = heard();
        tank.subscribe(new java.util.function.LongConsumer() {
            boolean registered;

            @Override
            public void accept(long litres) {             // registers the pump controller on its first reading
                panel.add(litres);
                if (!registered) {
                    registered = true;
                    tank.subscribe(pump::add);
                }
            }
        });

        onSensorThread(() -> {
            tank.update(7_200);
            tank.update(7_350);
        });

        assertThat(panel).containsExactly(7_200L, 7_350L);
        assertThat(pump).containsExactly(7_350L);
    }

    @Test
    void eachSubscriberHearsTheReadingItIsToldAbout() throws Exception {
        TankLevel tank = new TankLevel();
        List<Long> alarm = heard();
        tank.subscribe(litres -> {                         // the overflow valve opens at 25,000 and reports the result
            if (litres >= 25_000) tank.update(21_000);
        });
        tank.subscribe(alarm::add);

        onSensorThread(() -> tank.update(25_400));

        assertThat(alarm).containsExactlyInAnyOrder(25_400L, 21_000L);
    }
}
