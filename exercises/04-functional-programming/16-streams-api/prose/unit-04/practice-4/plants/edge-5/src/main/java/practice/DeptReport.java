package practice;

import java.util.*;
import java.util.stream.Collectors;

public final class DeptReport {

    private DeptReport() {
    }

    public record Employee(String name, String dept, int salary) {
    }

    public static Map<String, Long> headcount(List<Employee> staff) {
        return staff.stream().collect(Collectors.groupingBy(Employee::dept, Collectors.mapping(Employee::name,
                Collectors.collectingAndThen(Collectors.toSet(), s -> (long) s.size()))));
    }
    public static Map<String, Double> averageSalary(List<Employee> staff) {
        return staff.stream().collect(Collectors.groupingBy(Employee::dept, Collectors.averagingInt(Employee::salary)));
    }

    public static Map<String, List<String>> namesByDept(List<Employee> staff) {
        return staff.stream().collect(Collectors.groupingBy(Employee::dept, Collectors.mapping(Employee::name, Collectors.toList())));
    }

    public static Map<Boolean, List<String>> splitByPay(List<Employee> staff, int threshold) {
        return staff.stream().collect(Collectors.partitioningBy(e -> e.salary() >= threshold,
                Collectors.mapping(Employee::name, Collectors.toList())));
    }

}
