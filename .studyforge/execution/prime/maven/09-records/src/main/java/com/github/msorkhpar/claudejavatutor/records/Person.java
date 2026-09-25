package com.github.msorkhpar.claudejavatutor.records;

public record Person(String name, int age) {
    public Person {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
    }

    public boolean isAdult() {
        return age >= 18;
    }

    public static Person createAdult(String name) {
        return new Person(name, 18);
    }
}