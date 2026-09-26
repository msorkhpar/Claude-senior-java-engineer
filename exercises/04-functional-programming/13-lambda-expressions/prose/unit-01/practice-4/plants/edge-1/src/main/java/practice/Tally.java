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
        return () -> count++;
    }

    /** A Supplier, written as a lambda, whose body is {@code this}. */
    public Supplier<Object> selfFromLambda() {
        return new Supplier<Object>() {
            @Override
            public Object get() {
                return this;
            }
        };
    }

    /** A Supplier, written as an anonymous inner class, whose get() returns {@code this}. */
    public Supplier<Object> selfFromAnonymousClass() {
        return new Supplier<Object>() {
            @Override
            public Object get() {
                return this;
            }
        };
    }
}

