package practice;

import java.util.Comparator;
import java.util.List;

/** An employee on the roster. */
record Employee(String name, String department, double salary) {
}

public final class Roster {

    private Roster() {
    }

    /** Names by department ascending, salary descending, then name ascending. */
    public static List<String> ranked(List<Employee> employees) {
        return employees.stream()
                .sorted(Comparator.comparing(Employee::department)
                        .thenComparingDouble(Employee::salary)
                        .reversed()
                        .thenComparing(Employee::name))
                .map(Employee::name)
                .toList();
    }
}
