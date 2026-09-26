package practice;

import java.util.concurrent.atomic.AtomicInteger;

public class Ticket {
    private static final AtomicInteger ISSUED = new AtomicInteger();

    private static int number;

    public Ticket() {
        number = ISSUED.incrementAndGet();
    }

    public static int issued() {
        return ISSUED.get();
    }

    public int number() {
        return number;
    }
}
