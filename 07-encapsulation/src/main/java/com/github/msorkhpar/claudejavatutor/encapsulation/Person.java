package com.github.msorkhpar.claudejavatutor.encapsulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Person {
    private String name;
    private int age;
    private List<String> hobbies = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.name = name.trim();
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Age must be between 0 and 150");
        }
        this.age = age;
    }

    public List<String> getHobbies() {
        return Collections.unmodifiableList(hobbies);
    }

    public void addHobby(String hobby) {
        if (hobby == null || hobby.trim().isEmpty()) {
            throw new IllegalArgumentException("Hobby cannot be null or empty");
        }
        hobbies.add(hobby.trim());
    }
}