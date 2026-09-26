package practice;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.Callable;

public final class TaskRunner {

    private TaskRunner() {
    }

    /** Calls {@code task} and returns a one-line report of its outcome. */
    public static String run(Callable<String> task) {
        try {
            return "done: " + task.call();
        } catch (FileNotFoundException e) {
            return "missing: " + e.getMessage();
        } catch (IOException e) {
            return "io: " + e.getMessage();
        } catch (Exception e) {
            return "failed: " + e.getMessage();
        }
    }
}
