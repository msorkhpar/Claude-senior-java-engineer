package practice;

public class Birds {

    public interface Swimmer {
        String name();

        default String move() {
            throw new UnsupportedOperationException("write Swimmer.move");
        }
    }

    public interface Flyer {
        String name();

        default String move() {
            throw new UnsupportedOperationException("write Flyer.move");
        }
    }

    public record Duck(String name) implements Swimmer, Flyer {
        @Override
        public String move() {
            throw new UnsupportedOperationException("write Duck.move");
        }
    }

    public record Penguin(String name) implements Swimmer {
    }

    public record Bat(String name) implements Flyer {
    }
}
