package practice;

public class Animal {

    public Animal(String name) {
        throw new UnsupportedOperationException("write the constructor");
    }

    public String getName() {
        throw new UnsupportedOperationException("write getName");
    }

    public String makeSound() {
        throw new UnsupportedOperationException("write makeSound");
    }

    public String describe() {
        throw new UnsupportedOperationException("write describe");
    }

    public static class Dog extends Animal {

        public Dog(String name, String breed) {
            super(name);
        }

        public String getBreed() {
            throw new UnsupportedOperationException("write getBreed");
        }

        public String fetch() {
            throw new UnsupportedOperationException("write fetch");
        }
    }
}
