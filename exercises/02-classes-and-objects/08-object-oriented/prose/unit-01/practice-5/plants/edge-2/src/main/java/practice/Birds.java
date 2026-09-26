package practice;

public class Birds {

    public interface Swimmer {
        String name();

        default String move() {
            return name() + " swims";
        }
    }

    public interface Flyer {
        String name();

        default String move() {
            return "flies";
        }
    }

    public record Duck(String name) implements Swimmer, Flyer {
        @Override
        public String move() {
            return name() + " swims and " + name() + " flies";
        }
    }

    public record Penguin(String name) implements Swimmer {
    }

    public record Bat(String name) implements Flyer {
    }
}
