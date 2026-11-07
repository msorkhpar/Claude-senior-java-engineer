package practice;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Semaphore;

/**
 * The seats of a rendering library's floating licence. A render task takes a seat before it
 * calls the library and gives it back afterwards. Render tasks run on the render ForkJoinPool,
 * and so do the tasks that finish a render and give its seat back.
 */
public class LicenceSeats {

    private final Semaphore seats;

    public LicenceSeats(int count) {
        this.seats = new Semaphore(count);
    }

    /** Takes one seat, waiting until one is free. */
    public void take() throws InterruptedException {
        ForkJoinPool.managedBlock(new ForkJoinPool.ManagedBlocker() {
            private boolean taken;

            @Override
            public boolean block() throws InterruptedException {
                if (!taken) {
                    seats.acquire();
                    taken = true;
                }
                return true;
            }

            @Override
            public boolean isReleasable() {
                return taken || (taken = seats.tryAcquire());
            }
        });
    }

    /** Gives one seat back. */
    public void giveBack() {
        seats.release();
    }

    /** How many seats are free right now. */
    public int free() {
        return seats.availablePermits();
    }
}
