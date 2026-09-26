package practice;

public class Ticket {
    private static int issued;

    private final int number;

    public Ticket() {
        this.number = nextNumber();
    }

    private static synchronized int nextNumber() {
        issued++;
        return issued;
    }

    public static synchronized int issued() {
        return issued;
    }

    public int number() {
        return number;
    }
}
