package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class GroupsTest {

    /**
     * A map whose get() and containsKey() line two worker threads up on their first lookup: each
     * looks, then waits (at most 3 s) until the other has also looked, so a look-up-then-put
     * solution always has both threads find the group missing. The first thread through then goes
     * on alone, and the second waits (at most 3 s) until the first has finished its join, so a
     * second put always replaces the first thread's list after it was used. computeIfAbsent never
     * calls these two.
     */
    private static final class SteppedMap extends ConcurrentHashMap<String, List<String>> {
        private final CyclicBarrier bothLooked = new CyclicBarrier(2);
        private final Set<Thread> workers = ConcurrentHashMap.newKeySet();
        private final List<Thread> both = new CopyOnWriteArrayList<>();
        private final AtomicInteger through = new AtomicInteger();

        private void step() {
            Thread me = Thread.currentThread();
            if (workers.remove(me)) {
                try {
                    bothLooked.await(3, TimeUnit.SECONDS);
                } catch (TimeoutException | BrokenBarrierException e) {
                    // the other thread never looked: go on alone
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                if (through.getAndIncrement() == 1) {
                    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
                    for (Thread other : both) {
                        while (other != me && other.getState() != Thread.State.TERMINATED
                                && System.nanoTime() < deadline) {
                            Thread.onSpinWait();
                        }
                    }
                }
            }
        }

        @Override
        public List<String> get(Object key) {
            List<String> seen = super.get(key);
            step();
            return seen;
        }

        @Override
        public boolean containsKey(Object key) {
            boolean seen = super.containsKey(key);
            step();
            return seen;
        }
    }

    private static String name(String s) {
        return new String(s.toCharArray());
    }

    @Test
    void joinsMembersToGroups() {
        Groups groups = new Groups(new ConcurrentHashMap<>());
        groups.join(name("chess"), name("ann"));
        groups.join(name("chess"), name("bob"));
        groups.join(name("go"), name("cy"));
        assertThat(groups.members(name("chess"))).containsExactly("ann", "bob");
        assertThat(groups.members(name("go"))).containsExactly("cy");
        assertThat(groups.members(name("poker"))).isEmpty();
    }

    @Test
    void simultaneousFirstJoinsBothKept() throws InterruptedException {
        SteppedMap map = new SteppedMap();
        Groups groups = new Groups(map);
        String[] people = {name("ann"), name("bob")};
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread[] threads = new Thread[2];
        for (int i = 0; i < 2; i++) {
            int me = i;
            threads[i] = new Thread(() -> groups.join(name("new"), people[me]));
            threads[i].setDaemon(true);
            threads[i].setUncaughtExceptionHandler((t, e) -> failure.set(e));
            map.workers.add(threads[i]);
            map.both.add(threads[i]);
        }
        threads[0].start();
        threads[1].start();
        threads[0].join(8_000);
        threads[1].join(8_000);
        assertThat(threads[0].isAlive() || threads[1].isAlive()).as("both joins finished").isFalse();
        assertThat(failure.get()).isNull();
        assertThat(groups.members(name("new"))).containsExactlyInAnyOrder("ann", "bob");
    }

    @Test
    void membersIsACopy() {
        Groups groups = new Groups(new ConcurrentHashMap<>());
        groups.join(name("chess"), name("ann"));
        groups.join(name("chess"), name("bob"));
        List<String> before = groups.members(name("chess"));
        groups.join(name("chess"), name("dee"));
        assertThat(before).containsExactly("ann", "bob");
        assertThat(groups.members(name("chess"))).containsExactly("ann", "bob", "dee");
    }

    @Test
    void eachAnswerIsItsOwnList() {
        Groups groups = new Groups(new ConcurrentHashMap<>());
        groups.join("chess", "ann");
        groups.join("go", "cy");
        List<String> chess = groups.members("chess");
        List<String> go = groups.members("go");
        assertThat(chess).as("an earlier answer is not changed by a later call").containsExactly("ann");
        assertThat(go).containsExactly("cy");
    }
}
