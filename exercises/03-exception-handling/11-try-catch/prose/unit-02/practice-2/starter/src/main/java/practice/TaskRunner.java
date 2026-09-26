package practice;

import java.util.concurrent.Callable;

public final class TaskRunner {

    private TaskRunner() {
    }

    /** Calls {@code task} and returns a one-line report of its outcome. */
    public static String run(Callable<String> task) {
        throw new UnsupportedOperationException("write run");
    }
}
