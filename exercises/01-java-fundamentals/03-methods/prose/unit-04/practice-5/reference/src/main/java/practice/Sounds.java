package practice;

public final class Sounds {

    private Sounds() {
    }

    /** An animal: it speaks once, or several times in a row. */
    public static class Animal {

        /** Returns this animal's sound. */
        public String speak() {
            return "...";
        }

        /** Returns the sound repeated {@code times} times, separated by spaces (an overload). */
        public String speak(int times) {
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < times; i++) {
                if (i > 0) {
                    out.append(' ');
                }
                out.append(speak());
            }
            return out.toString();
        }
    }

    /** A dog: it overrides speak(). */
    public static class Dog extends Animal {

        /** Returns a dog's sound. */
        @Override
        public String speak() {
            return "Woof";
        }
    }
}
