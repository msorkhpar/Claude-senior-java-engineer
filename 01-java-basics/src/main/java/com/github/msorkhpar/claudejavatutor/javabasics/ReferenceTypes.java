// File: src/main/java/com/github/msorkhpar/claudejavatutor/javabasics/ReferenceTypes.java

package com.github.msorkhpar.claudejavatutor.javabasics;

import java.io.*;
import java.util.*;

public class ReferenceTypes {

    // Class example
    public static class Person implements Serializable {
        private String name;
        private int age;
        private List<Person> friends;

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
            this.friends = new ArrayList<>();
        }

        public void addFriend(Person friend) {
            friends.add(friend);
        }

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
            this.age = age;
        }

        public List<Person> getFriends() {
            return friends;
        }

        public void setFriends(List<Person> friends) {
            this.friends = friends;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Person person = (Person) o;
            return age == person.age && Objects.equals(name, person.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }
    }

    // Interface example
    public interface Printable {
        void print();
    }

    // Enum example
    public enum DayOfWeek {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }

    // Record example (JEP 395)
    public record PersonRecord(String name, int age) implements Printable {
        @Override
        public void print() {
            System.out.println("Name: " + name + ", Age: " + age);
        }
    }

    // Demonstrate null reference
    public static void demonstrateNullReference() {
        Person nullPerson = null;
        try {
            nullPerson.getName(); // This will throw NPE
        } catch (NullPointerException e) {
            System.out.println("Null Pointer Exception caught");
        }
    }

    // Demonstrate memory leak
    public static void demonstrateMemoryLeak() {
        List<Person> people = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            people.add(new Person("Person " + i, i));
        }
        // Uncomment the next line to fix the memory leak
        // people.clear();
    }

    // Demonstrate aliasing
    public static void demonstrateAliasing() {
        Person person1 = new Person("Alice", 30);
        Person person2 = person1;
        person2.setAge(31);
        System.out.println("Person1 age: " + person1.getAge()); // Will print 31
    }

    // Demonstrate pattern matching for instanceof (JEP 394)
    public static void printPersonInfo(Object obj) {
        if (obj instanceof Person person) {
            System.out.println("Name: " + person.getName() + ", Age: " + person.getAge());
        } else if (obj instanceof PersonRecord record) {
            System.out.println("Name: " + record.name() + ", Age: " + record.age());
        }
    }

    // Demonstrate circular reference
    public static void demonstrateCircularReference() {
        Person alice = new Person("Alice", 30);
        Person bob = new Person("Bob", 32);
        alice.addFriend(bob);
        bob.addFriend(alice);
    }

    // Demonstrate deep vs shallow copy
    public static Person shallowCopy(Person original) {
        return new Person(original.getName(), original.getAge());
    }

    public static Person deepCopy(Person original) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(original);

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bis);
        return (Person) in.readObject();
    }

    // Demonstrate proper null checking and Optional usage
    public static String getPersonName(Person person) {
        return Optional.ofNullable(person)
                .map(Person::getName)
                .orElse("Unknown");
    }

    // Demonstrate garbage collection
    public static void demonstrateGarbageCollection() {
        Person person = new Person("Temporary", 0);
        person = null; // The Person object is now eligible for garbage collection
        System.gc(); // Suggest garbage collection (for demonstration purposes only)
    }

    // Sealed class hierarchy example
    public abstract sealed class Shape permits Circle, Square, Triangle {
        private final String color;

        public Shape(String color) {
            this.color = color;
        }

        public String getColor() {
            return color;
        }

        public abstract double area();
    }

    public final class Circle extends Shape {
        private final double radius;

        public Circle(String color, double radius) {
            super(color);
            this.radius = radius;
        }

        @Override
        public double area() {
            return Math.PI * radius * radius;
        }
    }

    public final class Square extends Shape {
        private final double side;

        public Square(String color, double side) {
            super(color);
            this.side = side;
        }

        @Override
        public double area() {
            return side * side;
        }
    }

    public non-sealed class Triangle extends Shape {
        private final double base;
        private final double height;

        public Triangle(String color, double base, double height) {
            super(color);
            this.base = base;
            this.height = height;
        }

        @Override
        public double area() {
            return 0.5 * base * height;
        }
    }

    // Method to demonstrate pattern matching with sealed classes
    public static String describeShape(Shape shape) {
        return switch (shape) {
            case Circle c -> "A circle with radius " + Math.sqrt(c.area() / Math.PI);
            case Square s -> "A square with side " + Math.sqrt(s.area());
            case Triangle t -> "A triangle with area " + t.area();
        };
    }
}