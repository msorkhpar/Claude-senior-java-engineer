package practice;

import java.util.List;
import java.util.stream.Collectors;

public class Pets {

    public static class Animal {
        public String sound() {
            return "The animal makes a sound";
        }
    }

    public static class Dog extends Animal {
        @Override
        public String sound() {
            return "The dog barks";
        }
    }

    public static class Cat extends Animal {
        @Override
        public String sound() {
            return "The cat meows";
        }
    }

    public static class Puppy extends Dog {
    }

    public static class Kitten extends Cat {
    }

    public static String chorus(List<? extends Animal> animals) {
        return animals.stream().map(Animal::sound).collect(Collectors.joining(" / "));
    }
}
