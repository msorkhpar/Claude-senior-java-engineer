package practice;

public class Zoo {

    public abstract static class Animal {
    }

    public static final class Dog extends Animal {
    }

    public static final class Cat extends Animal {
    }

    public static class Bird extends Animal {
    }

    public static class Parrot extends Bird {
    }

    public static String sound(Animal animal) {
        if (animal instanceof Dog) {
            return "Woof";
        }
        if (animal instanceof Cat) {
            return "Meow";
        }
        if (animal instanceof Parrot) {
            return "Squawk";
        }
        return "Tweet";
    }
}
