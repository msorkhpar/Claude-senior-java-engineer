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
        // TODO: When every seat is taken, the render pool freezes: the tasks waiting for a seat
        //       hold all of its threads, and the tasks that would give seats back never start.
        //       Take exactly one seat, and wait in a way that lets the pool keep working.
        throw new IllegalStateException("TODO: take");
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
