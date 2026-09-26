package practice;

public class Animals {

    public static class Animal {
        protected final String name;

        public Animal(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String makeSound() {
            return "The animal makes a sound";
        }
    }

    public static class Dog extends Animal {

        public Dog(String name) {
            super(name);
        }

        @Override
        public String makeSound() {
            return name + " barks: Woof! Woof!";
        }

        /** The sound Animal's own version makes. */
        public String makeAnimalSound() {
            return super.makeSound();
        }

        public String wagTail() {
            return name + " is wagging its tail";
        }
    }
}
