package practice;

public record Person(String name, int age) {

    /** Refuse a null or empty name and a negative age, with IllegalArgumentException. */
    public Person {
        throw new UnsupportedOperationException("write the compact constructor");
    }
}
