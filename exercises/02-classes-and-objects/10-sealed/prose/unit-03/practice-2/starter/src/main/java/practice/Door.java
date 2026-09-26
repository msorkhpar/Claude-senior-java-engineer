package practice;

public class Door {

    public sealed interface State permits Opened, Closed, Locked {
    }

    public record Opened() implements State {
    }

    public record Closed() implements State {
    }

    public record Locked(String code) implements State {
    }

    public static State open(State s) {
        throw new UnsupportedOperationException("write open");
    }

    public static State close(State s) {
        throw new UnsupportedOperationException("write close");
    }

    public static State lock(State s, String code) {
        throw new UnsupportedOperationException("write lock");
    }

    public static State unlock(State s, String code) {
        throw new UnsupportedOperationException("write unlock");
    }
}
