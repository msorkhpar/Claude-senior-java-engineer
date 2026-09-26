package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.UnaryOperator;

public final class ResultLog {

    private final ReentrantLock lock = new ReentrantLock();
    private final List<String> results = new ArrayList<>();

    /** Transforms the input outside the lock, then appends the result under the lock and returns it. */
    public String processAndStore(String input, UnaryOperator<String> transform) {
        String result = transform.apply(input);
        lock.lock();
        try {
            results.add(result);
        } finally {
            lock.unlock();
        }
        return result;
    }

    /** The stored results, in order, as a copy the caller owns. */
    public List<String> results() {
        lock.lock();
        try {
            return results;
        } finally {
            lock.unlock();
        }
    }
}
