package com.github.msorkhpar.claudejavatutor.classobj;

public class Person {
    // Fields (Instance Variables)
    private String name;
    private int age;

    // Static field
    private static int personCount = 0;

    // Constructor
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
        personCount++;
    }

    // Methods
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        if (age >= 0) {
            this.age = age;
        } else {
            throw new IllegalArgumentException("Age cannot be negative");
        }
    }

    public void introduce() {
        System.out.println("Hello, my name is " + name + " and I am " + age + " years old.");
    }

    // Static method
    public static int getPersonCount() {
        return personCount;
    }

    // Nested class
    public static class Address {
        private String street;
        private String city;

        public Address(String street, String city) {
            this.street = street;
            this.city = city;
        }

        public String getFullAddress() {
            return street + ", " + city;
        }
    }
}