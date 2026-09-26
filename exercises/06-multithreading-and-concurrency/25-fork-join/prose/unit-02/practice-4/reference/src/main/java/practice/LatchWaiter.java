package practice;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;

public final class LatchWaiter {

    private LatchWaiter() {
    }

    /** Waits, through ForkJoinPool.managedBlock, until the latch reaches zero. */
    public static void await(CountDownLatch latch) throws InterruptedException {
        ForkJoinPool.managedBlock(new Blocker(latch));
    }

    private static final class Blocker implements ForkJoinPool.ManagedBlocker {
        private final CountDownLatch latch;

        Blocker(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public boolean block() throws InterruptedException {
            latch.await();
            return true;
        }

        @Override
        public boolean isReleasable() {
            return latch.getCount() == 0;
        }
    }
}
