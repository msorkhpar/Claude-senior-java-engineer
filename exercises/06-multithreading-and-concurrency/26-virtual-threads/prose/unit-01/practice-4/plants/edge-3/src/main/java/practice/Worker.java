package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

public final class Worker {
    private Worker() {
    }
    public static int drain(BlockingQueue<String> in, Consumer<String> sink) {
        int n = 0;
        while (true) {
            try {
                sink.accept(in.take());
                n++;
            } catch (InterruptedException e) {
                List<String> rest = new ArrayList<>();
                in.drainTo(rest);
                for (String s : rest) {
                    sink.accept(s);
                    n++;
                }
                Thread.currentThread().interrupt();
                return n;
            }
        }
    }
}
