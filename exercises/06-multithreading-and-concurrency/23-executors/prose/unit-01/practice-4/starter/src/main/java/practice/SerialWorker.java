package practice;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public final class SerialWorker implements AutoCloseable {

    public SerialWorker(String name) {
    }

    /** Queues a task; tasks run one at a time, in submission order. */
    public <T> Future<T> submit(Callable<T> task) {
        throw new UnsupportedOperationException("write submit");
    }

    /** Accepts no more tasks and returns once every submitted task has run. */
    @Override
    public void close() {
        throw new UnsupportedOperationException("write close");
    }
}
