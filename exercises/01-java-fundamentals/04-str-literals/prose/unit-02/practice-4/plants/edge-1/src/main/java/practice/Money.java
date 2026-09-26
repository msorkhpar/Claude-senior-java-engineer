package practice;

public final class Money {

    private Money() {
    }

    /** Returns the name and the amount as $dollars.cents. */
    public static String format(String name, long cents) {
        return name + ": $" + cents / 100 + "." + cents % 100;
    }
}
