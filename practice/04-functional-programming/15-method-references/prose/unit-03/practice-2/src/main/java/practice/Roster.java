package practice;

import java.util.List;

/** An employee on the roster. */
record Employee(String name, String department, double salary) {
}

public final class Roster {

    private Roster() {
    }

    /** Names by department ascending, salary descending, then name ascending. */
    public static List<String> ranked(List<Employee> employees) {
        throw new UnsupportedOperationException("write ranked");
    }
}
