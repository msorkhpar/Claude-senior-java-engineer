package practice;

public final class Greeters {

    private Greeters() {
    }

    @FunctionalInterface
    public interface Greeter {

        String greet(String name);

        /** Greets {@code title + " " + name} with this greeter's own greet. */
        default String greetWithTitle(String title, String name) {
            throw new UnsupportedOperationException("write greetWithTitle");
        }

        /** A greeter that says "Hello, <name>!". */
        static Greeter standard() {
            throw new UnsupportedOperationException("write standard");
        }
    }

    public interface English {
        default String hello() {
            return "Hello";
        }
    }

    public interface French {
        default String hello() {
            return "Bonjour";
        }
    }

    public static final class Bilingual implements English, French {

        /** Both inherited greetings, English first: "Hello / Bonjour". */
        @Override
        public String hello() {
            throw new UnsupportedOperationException("write hello");
        }
    }
}
