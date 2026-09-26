package practice;

public class Zoo {

    /** Seal me: exactly Dog, Cat and Bird may extend me. */
    public abstract static class Animal {
    }

    public static class Dog extends Animal {
    }

    public static class Cat extends Animal {
    }

    public static class Bird extends Animal {
    }

    /** Must keep compiling: a parrot is a bird. */
    public static class Parrot extends Bird {
    }

    /** Dog: Woof, Cat: Meow, Parrot: Squawk, any other Bird: Tweet. */
    public static String sound(Animal animal) {
        throw new UnsupportedOperationException("write sound");
    }
}
