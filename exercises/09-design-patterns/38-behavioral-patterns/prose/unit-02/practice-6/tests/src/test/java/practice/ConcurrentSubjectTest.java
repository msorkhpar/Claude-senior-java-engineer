package practice;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import practice.ConcurrentSubject.Observer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class ConcurrentSubjectTest {

    @Test
    void notifiesEveryObserverOnce() {
        ConcurrentSubject<Integer> subject = new ConcurrentSubject<>();
        List<String> first = new CopyOnWriteArrayList<>();
        List<String> second = new CopyOnWriteArrayList<>();
        Observer<Integer> one = (event, data) -> first.add(event + data);
        subject.addObserver(one);
        subject.addObserver(one);
        subject.addObserver((event, data) -> second.add(event + data));

        subject.notifyObservers("tick", 1);
        subject.removeObserver(one);
        subject.notifyObservers("tick", 2);

        assertThat(first).containsExactly("tick1");
        assertThat(second).containsExactly("tick1", "tick2");
        assertThat(subject.observerCount()).isEqualTo(1);
        assertThatThrownBy(() -> subject.addObserver(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aSubscriberDoesNotWaitForARunningNotification() throws Exception {
        Race race = Race.run();

        assertThat(race.subscriberFinishedFirst).isTrue();
    }

    @Test
    void aSubscriptionDuringANotificationDoesNotBreakIt() throws Exception {
        Race race = Race.run();

        assertThat(race.notifierFailure.get()).isNull();
        assertThat(race.later).containsExactly("tick1", "tick2");
        assertThat(race.newcomer).containsExactly("tick2");
    }

    /**
     * Thread A notifies and is held inside the first observer. Thread B then subscribes a newcomer.
     * The test waits until B has finished or is stuck waiting for a lock that A holds, and only then lets A go on.
     */
    static final class Race {
        final List<String> later = new CopyOnWriteArrayList<>();
        final List<String> newcomer = new CopyOnWriteArrayList<>();
        final AtomicReference<Throwable> notifierFailure = new AtomicReference<>();
        boolean subscriberFinishedFirst;

        static Race run() throws InterruptedException {
            Race race = new Race();
            ConcurrentSubject<Integer> subject = new ConcurrentSubject<>();
            CountDownLatch inside = new CountDownLatch(1);
            CountDownLatch release = new CountDownLatch(1);
            subject.addObserver((event, data) -> {
                if (data == 1) {
                    inside.countDown();
                    try {
                        release.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            subject.addObserver((event, data) -> race.later.add(event + data));

            Thread a = new Thread(() -> subject.notifyObservers("tick", 1));
            a.setUncaughtExceptionHandler((t, e) -> race.notifierFailure.set(e));
            a.start();
            inside.await();
            Observer<Integer> late = (event, data) -> race.newcomer.add(event + data);
            Thread b = new Thread(() -> subject.addObserver(late));
            b.start();
            while (b.isAlive() && !blockedBy(b, a)) {
                Thread.onSpinWait();
            }
            race.subscriberFinishedFirst = !b.isAlive();
            release.countDown();
            a.join();
            b.join();

            subject.notifyObservers("tick", 2);
            return race;
        }

        /** True when {@code t} is waiting for a lock that {@code owner} holds. */
        private static boolean blockedBy(Thread t, Thread owner) {
            ThreadInfo info = ManagementFactory.getThreadMXBean().getThreadInfo(t.threadId());
            return info != null && info.getLockOwnerId() == owner.threadId();
        }
    }
}
