package practice;

import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

public final class Worker {

    private Worker() {
    }

    /** Hands queued items to the sink until interrupted; returns how many, interrupt status kept. */
    public static int drain(BlockingQueue<String> in, Consumer<String> sink) {
        throw new UnsupportedOperationException("write drain");
    }
}
