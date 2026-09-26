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
        Integer[] results = new Integer[inputs.size()];
        for (int i = 0; i < inputs.size(); i++) {
            int slot = i;
            Thread worker = new Thread(() -> results[slot] = f.apply(inputs.get(slot)), "fanout-" + i);
            worker.start();
            worker.join();
        }
        return Arrays.asList(results);
    }
}
