package practice;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class DeptReport {

    private DeptReport() {
    }

    public record Employee(String name, String dept, int salary) {
    }

    /** The number of employees per department. */
    public static Map<String, Long> headcount(List<Employee> staff) {
        return staff.stream().collect(Collectors.groupingBy(Employee::dept, IdentityHashMap::new, Collectors.counting()));
    }

    /** The average salary per department. */
    public static Map<String, Double> averageSalary(List<Employee> staff) {
        return staff.stream().collect(Collectors.groupingBy(Employee::dept, Collectors.averagingInt(Employee::salary)));
    }

    /** The employees' names per department, in list order. */
    public static Map<String, List<String>> namesByDept(List<Employee> staff) {
        return staff.stream().collect(Collectors.groupingBy(Employee::dept, Collectors.mapping(Employee::name, Collectors.toList())));
    }

    /** Names of those paid at least {@code threshold} under true, the others under false, in list order. */
    public static Map<Boolean, List<String>> splitByPay(List<Employee> staff, int threshold) {
        return staff.stream().collect(Collectors.partitioningBy(e -> e.salary() >= threshold,
                Collectors.mapping(Employee::name, Collectors.toList())));
    }
}
