package practice;

import org.junit.jupiter.api.Test;

import java.lang.ref.Cleaner;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class NativeBufferTest {

    /** Hands out made-up addresses and records every free, from whichever thread it comes. */
    static final class RecordingAllocator implements NativeBuffer.Allocator {
        private final AtomicLong next = new AtomicLong(0x4000_0000L);
        final BlockingQueue<Long> freed = new LinkedBlockingQueue<>();

        @Override
        public long allocate(int bytes) {
            return next.getAndAdd(bytes + 4096L);
        }

        @Override
        public void free(long address) {
            freed.add(address);
        }

        long freesOf(long address) {
            return freed.stream().filter(a -> a == address).count();
        }
    }

    private static Cleaner daemonCleaner() {
        return Cleaner.create(r -> {
            Thread t = new Thread(r, "test-cleaner");
            t.setDaemon(true);
            return t;
        });
    }

    /** Asks for collections until the reference is cleared or ten seconds pass. */
    private static boolean collectUntilCleared(WeakReference<?> ref) throws InterruptedException {
        ReferenceQueue<Object> queue = new ReferenceQueue<>();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (ref.get() != null && System.nanoTime() < deadline) {
            WeakReference<Object> marker = new WeakReference<>(new Object(), queue);
            System.gc();
            queue.remove(200);
            Reference.reachabilityFence(marker);
        }
        return ref.get() == null;
    }

    /** Opens a buffer, forgets to close it, and returns its address. */
    private static long openAndForget(Cleaner cleaner, RecordingAllocator allocator, List<WeakReference<?>> refs) {
        NativeBuffer buffer = new NativeBuffer(cleaner, allocator, 8192);
        refs.add(new WeakReference<>(buffer));
        return buffer.address();
    }

    /** Opens a buffer, closes it, lets go of it, and returns its address. */
    private static long openCloseAndDrop(Cleaner cleaner, RecordingAllocator allocator, List<WeakReference<?>> refs) {
        NativeBuffer buffer = new NativeBuffer(cleaner, allocator, 2048);
        buffer.close();
        refs.add(new WeakReference<>(buffer));
        return buffer.address();
    }

    @Test
    void closingGivesTheMemoryBack() {
        RecordingAllocator allocator = new RecordingAllocator();
        long address;
        try (NativeBuffer buffer = new NativeBuffer(daemonCleaner(), allocator, 512)) {
            address = buffer.address();
            assertThat(allocator.freed).isEmpty();
        }
        assertThat(allocator.freesOf(address)).isEqualTo(1);
    }

    @Test
    void closingTwiceFreesOnce() {
        RecordingAllocator allocator = new RecordingAllocator();
        NativeBuffer buffer = new NativeBuffer(daemonCleaner(), allocator, 1024);

        buffer.close();
        buffer.close();

        assertThat(allocator.freesOf(buffer.address())).isEqualTo(1);
        assertThat(allocator.freed).hasSize(1);
    }

    @Test
    void aBufferNobodyClosedIsFreedOnceCollected() throws InterruptedException {
        RecordingAllocator allocator = new RecordingAllocator();
        List<WeakReference<?>> refs = new ArrayList<>();
        Cleaner cleaner = daemonCleaner();
        long address = openAndForget(cleaner, allocator, refs);

        Long freed = null;
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (freed == null && System.nanoTime() < deadline) {
            System.gc();
            freed = allocator.freed.poll(200, TimeUnit.MILLISECONDS);
        }

        assertThat(freed).as("the forgotten buffer's memory is freed after a collection").isEqualTo(address);
        Reference.reachabilityFence(cleaner);
    }

    @Test
    void aClosedBufferIsNotFreedAgainWhenCollected() throws InterruptedException {
        RecordingAllocator allocator = new RecordingAllocator();
        List<WeakReference<?>> refs = new ArrayList<>();
        Cleaner cleaner = daemonCleaner();
        long address = openCloseAndDrop(cleaner, allocator, refs);
        assertThat(allocator.freed.poll()).isEqualTo(address);

        assertThat(collectUntilCleared(refs.get(0))).as("the closed buffer is collected").isTrue();
        Long again = allocator.freed.poll(2, TimeUnit.SECONDS);

        assertThat(again).as("a second free of the closed buffer's address").isNull();
        Reference.reachabilityFence(cleaner);
    }
}
