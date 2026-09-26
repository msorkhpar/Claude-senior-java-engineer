package practice;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

record Person(String name, int age, String department) {
}

public final class Roster {

    private static final Comparator<Person> BY_DEPARTMENT_AGE_NAME = Comparator
            .comparing(Person::department)
            .thenComparingInt(Person::age);

    private static final Comparator<Person> OLDEST_FIRST =
            Comparator.comparingInt(Person::age).reversed();

    private Roster() {
    }

    public static Comparator<Person> byDepartmentAgeName() {
        return BY_DEPARTMENT_AGE_NAME;
    }

    public static Comparator<Person> oldestFirst() {
        return OLDEST_FIRST;
    }

    public static List<Person> sorted(List<Person> people) {
        List<Person> copy = new ArrayList<>(people);
        copy.sort(Comparator.nullsFirst(BY_DEPARTMENT_AGE_NAME));
        return copy;
    }
}
