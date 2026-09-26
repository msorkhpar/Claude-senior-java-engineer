package practice;

public class Commands {

    public sealed interface Command permits Quit, Echo, Extension {
    }

    public record Quit() implements Command {
    }

    public record Echo(String text) implements Command {
    }

    /** The extension point: anyone may extend it. */
    public abstract static non-sealed class Extension implements Command {
        public abstract String name();

        public abstract String run();
    }

    public static final class Help extends Extension {
        @Override
        public String name() {
            return "help";
        }

        @Override
        public String run() {
            return "commands: quit, echo";
        }
    }

    public static String execute(Command c) {
        return switch (c) {
            case Quit q -> "bye";
            case Echo e -> e.text();
            case Extension x -> "[" + x.name() + "] " + x.run();
        };
    }
}
