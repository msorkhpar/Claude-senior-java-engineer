package practice;

public class Animals {

    public static class Animal {
        protected final String name;

        public Animal(String name) {
            throw new UnsupportedOperationException("write the Animal constructor");
        }

        public String getName() {
            throw new UnsupportedOperationException("write getName");
        }

        public String makeSound() {
            throw new UnsupportedOperationException("write Animal.makeSound");
        }
    }

    public static class Dog extends Animal {

        public Dog(String name) {
            super(null); // replace: hand Animal what it needs
            throw new UnsupportedOperationException("write the Dog constructor");
        }

        @Override
        public String makeSound() {
            throw new UnsupportedOperationException("write Dog.makeSound");
        }

        /** The sound Animal's own version makes. */
        public String makeAnimalSound() {
            throw new UnsupportedOperationException("write makeAnimalSound");
        }

        public String wagTail() {
            throw new UnsupportedOperationException("write wagTail");
        }
    }
}
