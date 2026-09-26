package practice;

/** Something that can describe itself in one printed line. */
interface Printable {
    String print();
}

public record Person(String name, int age) implements Printable {

    /** True when this person is 18 or older. */
    public boolean isAdult() {
        throw new UnsupportedOperationException("write isAdult");
    }

    /** A person of this name, aged 18. */
    public static Person createAdult(String name) {
        throw new UnsupportedOperationException("write createAdult");
    }

    /** The line "Person: <name>, <age> years old". */
    @Override
    public String print() {
        throw new UnsupportedOperationException("write print");
    }

    /** "Person named <name> is <age> years old". */
    @Override
    public String toString() {
        throw new UnsupportedOperationException("write toString");
    }
}
