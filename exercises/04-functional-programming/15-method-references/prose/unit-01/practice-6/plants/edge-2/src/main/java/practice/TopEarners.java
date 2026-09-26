package practice;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** An employee of the company. */
record Employee(String name, String department, double salary, boolean active) {
}

public final class TopEarners {

    private TopEarners() {
    }

    /** Names of the best-paid active employees, highest salary first, at most limit of them. */
    public static List<String> names(List<Employee> employees, int limit) {
        return employees.stream()
                .filter(Employee::active)
                .sorted(Comparator.comparingDouble(Employee::salary).reversed())
                .limit(limit)
                .map(Employee::name)
                .toList();
    }

    /** Each department's average salary over its active employees. */
    public static Map<String, Double> averageSalaryByDepartment(List<Employee> employees) {
        return employees.stream()
                .collect(Collectors.groupingBy(Employee::department,
                        Collectors.averagingDouble(Employee::salary)));
    }
}
