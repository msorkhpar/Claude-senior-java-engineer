package practice;

import java.io.Serial;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The product is licensed for 25 concurrent seats, and one SeatLicense per process counts them.
 * The license is Serializable because user sessions hold it, and the session store writes
 * sessions out and reads them back.
 */
public final class SeatLicense implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final int SEATS = 25;

    private final AtomicInteger free = new AtomicInteger(SEATS);

    private SeatLicense() {
        checkConstruction();
    }

    private static final class Holder {
        static final SeatLicense INSTANCE = new SeatLicense();
    }

    public static SeatLicense getInstance() {
        return Holder.INSTANCE;
    }

    /** Takes a seat if one is free. */
    public boolean claim() {
        while (true) {
            int now = free.get();
            if (now == 0) return false;
            if (free.compareAndSet(now, now - 1)) return true;
        }
    }

    public void release() {
        free.incrementAndGet();
    }

    public int free() {
        return free.get();
    }

    /** Runs every time the constructor runs, whoever calls it. */
    private static void checkConstruction() {
        // TODO: getInstance() is not the only code that runs this constructor. Make sure that
        //       one SeatLicense at most ever comes out of it, whoever calls it and whenever.
        throw new IllegalStateException("TODO: checkConstruction");
    }

    /** What a session that held this license gets back when the store reads it. */
    @Serial
    private Object readResolve() {
        // TODO: A session that is read back must keep counting seats against the same license
        //       as every other session in this process, with the seats claimed as they are now.
        throw new IllegalStateException("TODO: readResolve");
    }
}
