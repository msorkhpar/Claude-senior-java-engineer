package practice;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;

import static org.assertj.core.api.Assertions.*;

class StrictMapTest {

    /** A HashMap that records, on each access, whether the calling thread holds the write or the read lock. */
    static final class Recording<K, V> extends HashMap<K, V> {
        final ReentrantReadWriteLock rw;
        final List<Boolean> accessesUnderWriteLock = new ArrayList<>();
        final List<Boolean> readsUnderReadLock = new ArrayList<>();
        boolean recording;

        Recording(ReentrantReadWriteLock rw) {
            this.rw = rw;
        }

        private void access() {
            if (recording) {
                accessesUnderWriteLock.add(rw.isWriteLockedByCurrentThread());
                readsUnderReadLock.add(rw.getReadHoldCount() > 0);
            }
        }

        @Override public V put(K key, V value) { access(); return super.put(key, value); }
        @Override public V remove(Object key) { access(); return super.remove(key); }
        @Override public V get(Object key) { access(); return super.get(key); }
        @Override public V getOrDefault(Object key, V d) { access(); return super.getOrDefault(key, d); }
        @Override public boolean containsKey(Object key) { access(); return super.containsKey(key); }
        @Override public int size() { access(); return super.size(); }
        @Override public Set<Map.Entry<K, V>> entrySet() { access(); return super.entrySet(); }
        @Override public Set<K> keySet() { access(); return super.keySet(); }
        @Override public Collection<V> values() { access(); return super.values(); }
        @Override public void forEach(BiConsumer<? super K, ? super V> action) { access(); super.forEach(action); }
    }

    /** A ReadWriteLock that counts how often its write lock is taken. */
    static final class Counting implements ReadWriteLock {
        final ReentrantReadWriteLock rw;
        int writeLocks;

        Counting(ReentrantReadWriteLock rw) {
            this.rw = rw;
        }

        @Override public Lock readLock() { return rw.readLock(); }

        @Override
        public Lock writeLock() {
            Lock w = rw.writeLock();
            return new Lock() {
                @Override public void lock() { writeLocks++; w.lock(); }
                @Override public void lockInterruptibly() throws InterruptedException { writeLocks++; w.lockInterruptibly(); }
                @Override public boolean tryLock() { writeLocks++; return w.tryLock(); }
                @Override public boolean tryLock(long t, TimeUnit u) throws InterruptedException { writeLocks++; return w.tryLock(t, u); }
                @Override public void unlock() { w.unlock(); }
                @Override public Condition newCondition() { return w.newCondition(); }
            };
        }
    }

    @Test
    void transferMovesTheValue() {
        StrictMap<String, Integer> m = new StrictMap<>(new HashMap<>(), new ReentrantReadWriteLock());
        m.put("a", 1000);
        m.transfer("a", "b");
        assertThat(m.get("b")).isEqualTo(1000);
        assertThat(m.get("a")).isNull();
        assertThat(m.snapshot()).containsExactly(Map.entry("b", 1000));
    }

    @Test
    void theWholeTransferHoldsTheWriteLock() {
        ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
        Recording<String, Integer> backing = new Recording<>(rw);
        Counting counting = new Counting(rw);
        StrictMap<String, Integer> m = new StrictMap<>(backing, counting);
        m.put("a", 1000);
        counting.writeLocks = 0;
        backing.recording = true;
        m.transfer("a", "b");
        backing.recording = false;
        assertThat(backing.accessesUnderWriteLock).isNotEmpty().containsOnly(true);
        assertThat(counting.writeLocks).isEqualTo(1);
        assertThat(m.get("b")).isEqualTo(1000);
    }

    @Test
    void aSnapshotIsTakenUnderTheReadLock() {
        ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
        Recording<String, Integer> backing = new Recording<>(rw);
        StrictMap<String, Integer> m = new StrictMap<>(backing, rw);
        m.put("a", 1);
        backing.recording = true;
        Map<String, Integer> snap = m.snapshot();
        backing.recording = false;
        assertThat(backing.readsUnderReadLock).isNotEmpty().containsOnly(true);
        assertThat(snap).containsExactly(Map.entry("a", 1));
    }

    @Test
    void aTransferOntoItselfKeepsTheValue() {
        StrictMap<String, Integer> m = new StrictMap<>(new HashMap<>(), new ReentrantReadWriteLock());
        m.put("a", 1);
        m.transfer("a", "a");
        assertThat(m.snapshot()).containsExactly(Map.entry("a", 1));
    }

    @Test
    void anAbsentKeyMovesNothing() {
        StrictMap<String, Integer> m = new StrictMap<>(new HashMap<>(), new ReentrantReadWriteLock());
        m.put("a", 1);
        m.transfer("x", "y");
        assertThat(m.snapshot()).containsOnlyKeys("a");
    }

    @Test
    void aSnapshotDoesNotFollowLaterWrites() {
        StrictMap<String, Integer> m = new StrictMap<>(new HashMap<>(), new ReentrantReadWriteLock());
        m.put("a", 1);
        Map<String, Integer> snap = m.snapshot();
        m.put("c", 3);
        m.transfer("a", "b");
        assertThat(snap).containsExactly(Map.entry("a", 1));
    }
}
