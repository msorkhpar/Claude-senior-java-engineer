package practice;

public record Person(String name, int age) {

    /** Refuse a null or empty name and a negative age, with IllegalArgumentException. */
    public Person {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
    }
}
