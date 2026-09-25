package com.github.msorkhpar.claudejavatutor.records;

public record PersonDTO(String name, int age, String email) {
    // Custom constructor with validation
    public PersonDTO {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    // Custom method
    public boolean isAdult() {
        return age >= 18;
    }
}