package practice;

public class Ticket {
    private static int issued;

    private final int number;

    public Ticket() {
        issued++;
        this.number = issued;
    }

    public static int issued() {
        return issued;
    }

    public int number() {
        return number;
    }
}
