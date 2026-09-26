package practice;

/** Something that can describe itself in one printed line. */
interface Printable {
    String print();
}

public record Person(String name, int age) implements Printable {

    /** True when this person is 18 or older. */
    public boolean isAdult() {
        return age >= 18;
    }

    /** A person of this name, aged 18. */
    public static Person createAdult(String name) {
        return new Person(name, 18);
    }

    /** The line "Person: <name>, <age> years old". */
    @Override
    public String print() {
        return "Person: " + name + ", " + age + " years old";
    }

    /** "Person named <name> is <age> years old". */
    @Override
    public String toString() {
        return "Person named " + name + " is " + age + " years old";
    }

    /** Two people are the same when they print the same. */
    @Override
    public boolean equals(Object other) {
        return other instanceof Person p && p.name.equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
