package practice;

import java.util.List;
import java.util.Map;

/** An employee of the company. */
record Employee(String name, String department, double salary, boolean active) {
}

public final class TopEarners {

    private TopEarners() {
    }

    /** Names of the best-paid active employees, highest salary first, at most limit of them. */
    public static List<String> names(List<Employee> employees, int limit) {
        throw new UnsupportedOperationException("write names");
    }

    /** Each department's average salary over its active employees. */
    public static Map<String, Double> averageSalaryByDepartment(List<Employee> employees) {
        throw new UnsupportedOperationException("write averageSalaryByDepartment");
    }
}
