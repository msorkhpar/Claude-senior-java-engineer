package practice;

import org.junit.jupiter.api.Test;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.*;

class SyncPurgeTest {

    /** A backing list that records, on each call made on it, whether the wrapper's lock is held by SyncPurge's own code. */
    static final class Probed extends ArrayList<String> {
        Object wrapper;
        final List<Boolean> heldByYou = new ArrayList<>();

        Probed(Collection<String> items) {
            super(items);
        }

        void probe() {
            if (wrapper == null) {
                return;
            }
            ThreadInfo info = ManagementFactory.getThreadMXBean()
                    .getThreadInfo(new long[]{Thread.currentThread().threadId()}, true, false)[0];
            boolean held = false;
            for (MonitorInfo m : info.getLockedMonitors()) {
                if (m.getIdentityHashCode() == System.identityHashCode(wrapper)
                        && m.getLockedStackFrame().getClassName().startsWith("practice.SyncPurge")) {
                    held = true;
                }
            }
            heldByYou.add(held);
        }

        @Override public int size() { probe(); return super.size(); }
        @Override public boolean isEmpty() { probe(); return super.isEmpty(); }
        @Override public String get(int i) { probe(); return super.get(i); }
        @Override public String set(int i, String s) { probe(); return super.set(i, s); }
        @Override public boolean add(String s) { probe(); return super.add(s); }
        @Override public boolean addAll(Collection<? extends String> c) { probe(); return super.addAll(c); }
        @Override public String remove(int i) { probe(); return super.remove(i); }
        @Override public boolean remove(Object o) { probe(); return super.remove(o); }
        @Override public boolean removeAll(Collection<?> c) { probe(); return super.removeAll(c); }
        @Override public boolean retainAll(Collection<?> c) { probe(); return super.retainAll(c); }
        @Override public boolean removeIf(Predicate<? super String> p) { probe(); return super.removeIf(p); }
        @Override public void clear() { probe(); super.clear(); }
        @Override public Object[] toArray() { probe(); return super.toArray(); }

        @Override
        public Iterator<String> iterator() {
            probe();
            Iterator<String> it = super.iterator();
            return new Iterator<>() {
                @Override public boolean hasNext() { probe(); return it.hasNext(); }
                @Override public String next() { probe(); return it.next(); }
                @Override public void remove() { probe(); it.remove(); }
            };
        }
    }

    @Test
    void removesTheDoomedElements() {
        List<String> list = Collections.synchronizedList(new ArrayList<>(List.of("a", "bb", "ccc")));
        SyncPurge.removeIf(list, s -> s.length() > 1);
        assertThat(list).containsExactly("a");
    }

    @Test
    void everyTestRunsUnderTheWrappersLock() {
        List<String> list = Collections.synchronizedList(new ArrayList<>(List.of("a", "bb", "ccc", "d")));
        List<Boolean> held = new ArrayList<>();
        SyncPurge.removeIf(list, s -> {
            held.add(Thread.holdsLock(list));
            return s.length() > 1;
        });
        assertThat(held).hasSize(4).containsOnly(true);
        assertThat(list).containsExactly("a", "d");
    }

    @Test
    void everyListCallIsInsideYourOwnSynchronizedBlock() {
        Probed backing = new Probed(List.of("a", "bb", "ccc", "d"));
        List<String> list = Collections.synchronizedList(backing);
        backing.wrapper = list;
        SyncPurge.removeIf(list, s -> s.length() > 1);
        backing.wrapper = null;
        assertThat(backing.heldByYou).isNotEmpty().containsOnly(true);
        assertThat(list).containsExactly("a", "d");
    }
}
