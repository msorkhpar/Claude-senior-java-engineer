package practice;

import java.util.List;

public class Pets {

    public static class Animal {
        public String sound() {
            throw new UnsupportedOperationException("write Animal.sound");
        }
    }

    public static class Dog extends Animal {
        // override sound()
    }

    public static class Cat extends Animal {
        // override sound()
    }

    public static class Puppy extends Dog {
    }

    public static class Kitten extends Cat {
        // override sound(), reusing Cat's
    }

    public static String chorus(List<? extends Animal> animals) {
        throw new UnsupportedOperationException("write chorus");
    }
}
