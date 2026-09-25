package practice;

public class TicketDispenser {

    private int next = 1;

    /** Hands out the next ticket number: 1 first, then 2, and so on. */
    public int take() {
        throw new UnsupportedOperationException("write take");
    }

    /** Skips the next ticket, and returns the number take() will hand out now. */
    public int skip() {
        throw new UnsupportedOperationException("write skip");
    }

    /** Returns the number take() will hand out next, without handing it out. */
    public int peek() {
        throw new UnsupportedOperationException("write peek");
    }
}
