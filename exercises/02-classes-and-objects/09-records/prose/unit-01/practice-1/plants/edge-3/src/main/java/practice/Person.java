package practice;

public record Person(String name, int age) {

    /** Refuse a null or empty name and a negative age, with IllegalArgumentException. */
    public Person {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
    }
}
