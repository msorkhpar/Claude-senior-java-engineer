package practice;

import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

public final class Worker {

    private Worker() {
    }

    /** Hands queued items to the sink until interrupted; returns how many, interrupt status kept. */
    public static int drain(BlockingQueue<String> in, Consumer<String> sink) {
        int handled = 0;
        while (!Thread.currentThread().isInterrupted()) {
            try {
                sink.accept(in.take());
                handled++;
            } catch (InterruptedException e) {
                // swallowed: the loop goes on
            }
        }
        return handled;
    }
}
