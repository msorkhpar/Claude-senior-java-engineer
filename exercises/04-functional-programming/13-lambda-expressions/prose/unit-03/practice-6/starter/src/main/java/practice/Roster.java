package practice;

import java.util.Comparator;
import java.util.List;

record Person(String name, int age, String department) {
}

public final class Roster {

    private Roster() {
    }

    public static Comparator<Person> byDepartmentAgeName() {
        throw new UnsupportedOperationException("write byDepartmentAgeName");
    }

    public static Comparator<Person> oldestFirst() {
        throw new UnsupportedOperationException("write oldestFirst");
    }

    public static List<Person> sorted(List<Person> people) {
        throw new UnsupportedOperationException("write sorted");
    }
}
