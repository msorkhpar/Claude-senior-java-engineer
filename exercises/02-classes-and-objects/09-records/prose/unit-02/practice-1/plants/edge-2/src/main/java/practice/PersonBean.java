package practice;

import java.util.Objects;

/** The traditional-class version of record Person(String name, int age). */
public final class PersonBean {

    private final String name;
    private final int age;

    public PersonBean(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String name() {
        return name;
    }

    public int age() {
        return age;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PersonBean that)) {
            return false;
        }
        return age == that.age && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return "PersonBean[name=" + name + ", age=" + age + "]";
    }
}
