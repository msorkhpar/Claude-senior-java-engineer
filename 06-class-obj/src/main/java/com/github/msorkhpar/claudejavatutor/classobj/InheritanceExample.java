package com.github.msorkhpar.claudejavatutor.classobj;

public class InheritanceExample {

    public static class Animal {
        protected String name;

        public Animal(String name) {
            this.name = name;
        }

        public void makeSound() {
            System.out.println("The animal makes a sound");
        }

        public String getName() {
            return name;
        }
    }

    public static class Dog extends Animal {
        private String breed;

        public Dog(String name, String breed) {
            super(name);
            this.breed = breed;
        }

        @Override
        public void makeSound() {
            System.out.println("The dog barks");
        }

        public void fetch() {
            System.out.println(name + " is fetching");
        }

        public String getBreed() {
            return breed;
        }
    }
}