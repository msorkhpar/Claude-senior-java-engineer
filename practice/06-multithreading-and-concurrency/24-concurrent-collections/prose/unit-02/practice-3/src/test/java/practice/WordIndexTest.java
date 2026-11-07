package practice;

import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class WordIndexTest {

    /** Holds one chosen thread once, at the first moment it reaches hold(), until resume opens. */
    static final class Hold {
        volatile Thread target;
        final CountDownLatch settled = new CountDownLatch(1);   // the target is held, or has finished
        final CountDownLatch resume = new CountDownLatch(1);
        private final AtomicBoolean held = new AtomicBoolean();

        void hold() {
            if (Thread.currentThread() == target && held.compareAndSet(false, true)) {
                settled.countDown();
                try {
                    resume.await(5, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /** A map that holds the chosen thread right after its first read (get, getOrDefault, containsKey). */
    static final class HoldingMap extends ConcurrentHashMap<String, Set<String>> {
        final Hold hold = new Hold();

        @Override
        public Set<String> get(Object key) {
            Set<String> v = super.get(key);
            hold.hold();
            return v;
        }

        @Override
        public Set<String> getOrDefault(Object key, Set<String> defaultValue) {
            Set<String> v = super.getOrDefault(key, defaultValue);
            hold.hold();
            return v;
        }

        @Override
        public boolean containsKey(Object key) {
            boolean b = super.containsKey(key);
            hold.hold();
            return b;
        }
    }

    /** A thread-safe set that holds the chosen thread inside its first add, before adding. */
    static final class HoldingSet extends ConcurrentSkipListSet<String> {
        final Hold hold = new Hold();

        @Override
        public boolean add(String s) {
            hold.hold();
            return super.add(s);
        }
    }

    private static ExecutorService daemons() {
        return Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
    }

    private static <T> T within(Callable<T> call) throws Exception {
        ExecutorService pool = daemons();
        try {
            return pool.submit(call).get(5, TimeUnit.SECONDS);
        } finally {
            pool.shutdownNow();
        }
    }

    private static void run(Runnable r) throws Exception {
        within(() -> {
            r.run();
            return null;
        });
    }

    /** Whether waiter is blocked on a monitor that holder owns. */
    private static boolean blockedBy(Thread waiter, Thread holder) {
        ThreadInfo info = ManagementFactory.getThreadMXBean().getThreadInfo(waiter.threadId());
        return info != null && info.getThreadState() == Thread.State.BLOCKED
                && info.getLockOwnerId() == holder.threadId();
    }

    @Test
    void indexesAndForgetsDocuments() throws Exception {
        WordIndex index = new WordIndex();
        run(() -> {
            index.add("tide", "doc-31");
            index.add("tide", "doc-44");
            index.add("reef", "doc-31");
            index.add("tide", "doc-44");
        });
        assertThat(index.documentsWith("tide")).containsExactlyInAnyOrder("doc-31", "doc-44");
        assertThat(index.documentsWith("reef")).containsExactly("doc-31");

        run(() -> index.remove("tide", "doc-31"));
        assertThat(index.documentsWith("tide")).containsExactly("doc-44");
        assertThat(index.knows("tide")).isTrue();

        run(() -> {
            index.remove("tide", "doc-44");
            index.remove("kelp", "doc-31");
        });
        assertThat(index.knows("tide")).as("a word no document has is dropped").isFalse();
        assertThat(index.documentsWith("tide")).isEmpty();
        assertThat(index.documentsWith("reef")).containsExactly("doc-31");
    }

    @Test
    void aSearchCanWalkAWordWhileItIsIndexed() throws Exception {
        WordIndex index = new WordIndex();
        run(() -> {
            index.add("moon", "doc-7");
            index.add("moon", "doc-8");
            index.add("moon", "doc-9");
        });

        List<String> walked = new ArrayList<>();
        Iterator<String> search = index.documentsWith("moon").iterator();
        walked.add(search.next());
        run(() -> index.add("moon", "doc-10"));          // another indexing thread, mid-walk
        while (search.hasNext()) walked.add(search.next());

        assertThat(walked).contains("doc-7", "doc-8", "doc-9");
        assertThat(index.documentsWith("moon")).contains("doc-10");
    }

    @Test
    void twoFirstDocumentsForANewWordAreBothKept() throws Exception {
        HoldingMap store = new HoldingMap();
        WordIndex index = new WordIndex(store);
        ExecutorService pool = daemons();
        try {
            Future<?> first = pool.submit(() -> {
                store.hold.target = Thread.currentThread();
                try {
                    index.add("comet", "doc-a");
                } finally {
                    store.hold.settled.countDown();
                }
            });
            assertThat(store.hold.settled.await(5, TimeUnit.SECONDS)).isTrue();
            pool.submit(() -> index.add("comet", "doc-b")).get(5, TimeUnit.SECONDS);
            store.hold.resume.countDown();
            first.get(5, TimeUnit.SECONDS);
        } finally {
            store.hold.resume.countDown();
            pool.shutdownNow();
        }

        assertThat(index.documentsWith("comet")).containsExactlyInAnyOrder("doc-a", "doc-b");
    }

    /**
     * The word has one document. One thread adds a second and is held inside the set's add; a
     * second thread removes the first document. The test waits until the remover has finished
     * or is blocked on a lock the adder holds, then lets the adder go on.
     */
    @Test
    void aDocumentAddedWhileTheLastIsRemovedIsKept() throws Exception {
        ConcurrentHashMap<String, Set<String>> store = new ConcurrentHashMap<>();
        HoldingSet nova = new HoldingSet();
        nova.add("doc-old");
        store.put("nova", nova);
        WordIndex index = new WordIndex(store);

        Thread adder = new Thread(() -> {
            nova.hold.target = Thread.currentThread();
            try {
                index.add("nova", "doc-new");
            } finally {
                nova.hold.settled.countDown();
            }
        });
        Thread remover = new Thread(() -> index.remove("nova", "doc-old"));
        adder.setDaemon(true);
        remover.setDaemon(true);
        try {
            adder.start();
            assertThat(nova.hold.settled.await(5, TimeUnit.SECONDS)).isTrue();
            remover.start();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
            while (System.nanoTime() < deadline && remover.isAlive() && !blockedBy(remover, adder)) {
                Thread.onSpinWait();
            }
        } finally {
            nova.hold.resume.countDown();
        }
        adder.join(5_000);
        remover.join(5_000);
        assertThat(adder.isAlive() || remover.isAlive()).as("both calls finished").isFalse();

        assertThat(index.knows("nova")).isTrue();
        assertThat(index.documentsWith("nova")).containsExactly("doc-new");
    }
}
