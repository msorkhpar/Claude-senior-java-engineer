package practice;

import java.util.List;
import java.util.stream.Collectors;

public class Pets {

    public static class Animal {
        public String sound() {
            return switch (getClass().getSimpleName()) {
                case "Dog" -> "The dog barks";
                case "Cat" -> "The cat meows";
                case "Kitten" -> "The cat meows softly";
                default -> "The animal makes a sound";
            };
        }
    }

    public static class Dog extends Animal {
    }

    public static class Cat extends Animal {
    }

    public static class Puppy extends Dog {
    }

    public static class Kitten extends Cat {
    }

    public static String chorus(List<? extends Animal> animals) {
        return animals.stream().map(Animal::sound).collect(Collectors.joining(" / "));
    }
}
