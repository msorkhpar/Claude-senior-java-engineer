package practice;

public class Zoo {

    public abstract static sealed class Animal permits Dog, Cat, Bird {
    }

    public static final class Dog extends Animal {
    }

    public static final class Cat extends Animal {
    }

    public static sealed class Bird extends Animal permits Parrot {
    }

    public static final class Parrot extends Bird {
    }

    public static String sound(Animal animal) {
        return switch (animal) {
            case Dog dog -> "Woof";
            case Cat cat -> "Meow";
            case Parrot parrot -> "Squawk";
            case Bird bird -> "Tweet";
        };
    }
}
