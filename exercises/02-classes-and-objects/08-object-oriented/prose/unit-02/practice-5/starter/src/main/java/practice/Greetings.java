package practice;

import java.util.List;

public class Greetings {

    public interface Greeter {
        String greet(String name);

        /** New: greets every name with this greeter's greet. Existing greeters must not break. */
        default List<String> greetAll(List<String> names) {
            throw new UnsupportedOperationException("write greetAll");
        }
    }

    public static class English implements Greeter {
        @Override
        public String greet(String name) {
            throw new UnsupportedOperationException("write English.greet");
        }
    }

    public static class French implements Greeter {
        @Override
        public String greet(String name) {
            throw new UnsupportedOperationException("write French.greet");
        }
    }

    public static class Shy implements Greeter {
        @Override
        public String greet(String name) {
            throw new UnsupportedOperationException("write Shy.greet");
        }

        @Override
        public List<String> greetAll(List<String> names) {
            throw new UnsupportedOperationException("write Shy.greetAll");
        }
    }
}
