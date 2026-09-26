package practice;

import java.util.List;

public class Greetings {

    public interface Greeter {
        String greet(String name);

        /** New: greets every name with this greeter's greet. Existing greeters must not break. */
        default List<String> greetAll(List<String> names) {
            return names.stream().map(this::greet).toList();
        }
    }

    public static class English implements Greeter {
        @Override
        public String greet(String name) {
            return "Hello, " + name + "!";
        }
    }

    public static class French implements Greeter {
        @Override
        public String greet(String name) {
            return "Bonjour, " + name + " !";
        }
    }

    public static class Shy implements Greeter {
        @Override
        public String greet(String name) {
            return "hi " + name;
        }

        @Override
        public List<String> greetAll(List<String> names) {
            return List.of(greet(names.get(0)));
        }
    }
}
