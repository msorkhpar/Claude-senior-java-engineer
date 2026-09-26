package practice;

public final class Sounds {

    private Sounds() {
    }

    /** An animal: it speaks once, or several times in a row. */
    public static class Animal {

        /** Returns this animal's sound. */
        public String speak() {
            throw new UnsupportedOperationException("write speak");
        }

        /** Returns the sound repeated {@code times} times, separated by spaces (an overload). */
        public String speak(int times) {
            throw new UnsupportedOperationException("write speak(int)");
        }
    }

    /** A dog: it overrides speak(). */
    public static class Dog extends Animal {

        /** Returns a dog's sound. */
        @Override
        public String speak() {
            throw new UnsupportedOperationException("write Dog.speak");
        }
    }
}
