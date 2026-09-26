package practice;

import java.util.ArrayDeque;
import java.util.Deque;

public final class CommandQueue {

    private final Deque<Runnable> queue = new ArrayDeque<>();

    public void enqueue(Runnable command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        queue.push(command);
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public boolean executeNext() {
        Runnable command = queue.poll();
        if (command == null) {
            return false;
        }
        command.run();
        return true;
    }

    public int executeAll() {
        int count = 0;
        while (executeNext()) {
            count++;
        }
        return count;
    }
}
