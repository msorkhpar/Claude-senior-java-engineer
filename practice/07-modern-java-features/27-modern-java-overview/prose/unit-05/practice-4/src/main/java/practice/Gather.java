package practice;

import java.util.List;
import java.util.concurrent.Callable;

public final class Gather {

    private Gather() {
    }

    /** One task's result: its value, or the message of the exception it threw. */
    public record Outcome<T>(T value, String error) {
    }

    /** Runs each task on its own virtual thread and returns the outcomes in task order. */
    public static <T> List<Outcome<T>> all(List<Callable<T>> tasks) {
        throw new UnsupportedOperationException("write all");
    }
}
