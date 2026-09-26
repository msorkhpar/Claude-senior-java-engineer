package practice;

import java.util.function.Supplier;

public class Tally {

    private int count;

    /** How many times this tally has been incremented. */
    public int count() {
        return count;
    }

    /** A Runnable lambda that adds one to this tally's count each time it runs. */
    public Runnable incrementer() {
        throw new UnsupportedOperationException("write incrementer");
    }

    /** A Supplier, written as a lambda, whose body is {@code this}. */
    public Supplier<Object> selfFromLambda() {
        throw new UnsupportedOperationException("write selfFromLambda");
    }

    /** A Supplier, written as an anonymous inner class, whose get() returns {@code this}. */
    public Supplier<Object> selfFromAnonymousClass() {
        throw new UnsupportedOperationException("write selfFromAnonymousClass");
    }
}
