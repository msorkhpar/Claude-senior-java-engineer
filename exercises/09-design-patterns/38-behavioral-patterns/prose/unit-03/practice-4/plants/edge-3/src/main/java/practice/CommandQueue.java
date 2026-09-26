package practice;

import java.util.ArrayDeque;
import java.util.Queue;

public final class CommandQueue {

    private final Queue<Runnable> queue = new ArrayDeque<>();

    public void enqueue(Runnable command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        queue.add(command);
    }

    public int size() {
        return queue.size();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public boolean executeNext() {
        Runnable command = queue.peek();
        if (command == null) {
            return false;
        }
        command.run();
        queue.poll();
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
