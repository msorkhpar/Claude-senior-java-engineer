package practice;

public final class Greeters {

    private Greeters() {
    }

    @FunctionalInterface
    public interface Greeter {

        String greet(String name);

        /** Greets {@code title + " " + name} with this greeter's own greet. */
        default String greetWithTitle(String title, String name) {
            return greet(title + " " + name);
        }

        /** A greeter that says "Hello, <name>!". */
        static Greeter standard() {
            return name -> "Hello, " + name + "!";
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
            return English.super.hello() + " / " + French.super.hello();
        }
    }
}
