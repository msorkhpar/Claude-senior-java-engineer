package com.github.msorkhpar.claudejavatutor.classobj;

import java.time.LocalDate;
import java.util.Objects;

public class Person2 {
    private final String name;
    private final LocalDate dateOfBirth;
    private String email;

    // Constructor with all fields
    public Person2(String name, LocalDate dateOfBirth, String email) {
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.dateOfBirth = Objects.requireNonNull(dateOfBirth, "Date of birth cannot be null");
        this.email = email;
    }

    // Constructor chaining - calls the full constructor with email as null
    public Person2(String name, LocalDate dateOfBirth) {
        this(name, dateOfBirth, null);
    }

    // Copy constructor
    public Person2(Person2 other) {
        this(other.name, other.dateOfBirth, other.email);
    }

    // Getters
    public String getName() {
        return name;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    // Setter for email (other fields are final)
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", email='" + email + '\'' +
                '}';
    }
}