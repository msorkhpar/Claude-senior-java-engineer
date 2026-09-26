package practice;

public class Ticket {
    private static int issued;

    private static int number;

    public Ticket() {
        number = nextNumber();
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
