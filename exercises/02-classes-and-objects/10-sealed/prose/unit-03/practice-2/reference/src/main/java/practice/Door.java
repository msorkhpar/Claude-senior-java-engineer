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
        return switch (s) {
            case Opened o -> o;
            case Closed c -> new Opened();
            case Locked l -> throw new IllegalStateException("a locked door does not open");
        };
    }

    public static State close(State s) {
        return switch (s) {
            case Opened o -> new Closed();
            case Closed c -> c;
            case Locked l -> l;
        };
    }

    public static State lock(State s, String code) {
        return switch (s) {
            case Opened o -> throw new IllegalStateException("close the door before locking it");
            case Closed c -> new Locked(code);
            case Locked l -> l;
        };
    }

    public static State unlock(State s, String code) {
        return switch (s) {
            case Locked l when l.code().equals(code) -> new Closed();
            case Opened o -> o;
            case Closed c -> c;
            case Locked l -> l;
        };
    }
}
