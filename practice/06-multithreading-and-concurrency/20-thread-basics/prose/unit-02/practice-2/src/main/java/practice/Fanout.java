package practice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public final class Fanout {

    private Fanout() {
    }

    /** Runs f on each input on its own thread, all at once, and returns the results in input order. */
    public static List<Integer> mapAll(List<Integer> inputs, Function<Integer, Integer> f) throws InterruptedException {
        throw new UnsupportedOperationException("write mapAll");
    }
}
