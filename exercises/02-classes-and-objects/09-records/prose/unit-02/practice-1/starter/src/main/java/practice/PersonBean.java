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
        throw new UnsupportedOperationException("write equals");
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("write hashCode");
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("write toString");
    }
}
