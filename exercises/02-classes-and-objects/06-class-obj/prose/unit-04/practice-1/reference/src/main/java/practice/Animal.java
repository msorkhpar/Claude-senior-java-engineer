package practice;

public class Animal {
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

    public String describe() {
        return name + " the animal";
    }

    public static class Dog extends Animal {
        private final String breed;

        public Dog(String name, String breed) {
            super(name);
            this.breed = breed;
        }

        public String getBreed() {
            return breed;
        }

        @Override
        public String makeSound() {
            return "The dog barks";
        }

        public String fetch() {
            return name + " is fetching";
        }

        @Override
        public String describe() {
            return super.describe() + " (" + breed + ")";
        }
    }
}
