package practice;

import java.util.List;
import java.util.Map;

public final class DeptReport {

    private DeptReport() {
    }

    public record Employee(String name, String dept, int salary) {
    }

    /** The number of employees per department. */
    public static Map<String, Long> headcount(List<Employee> staff) {
        throw new UnsupportedOperationException("write headcount");
    }

    /** The average salary per department. */
    public static Map<String, Double> averageSalary(List<Employee> staff) {
        throw new UnsupportedOperationException("write averageSalary");
    }

    /** The employees' names per department, in list order. */
    public static Map<String, List<String>> namesByDept(List<Employee> staff) {
        throw new UnsupportedOperationException("write namesByDept");
    }

    /** Names of those paid at least {@code threshold} under true, the others under false, in list order. */
    public static Map<Boolean, List<String>> splitByPay(List<Employee> staff, int threshold) {
        throw new UnsupportedOperationException("write splitByPay");
    }
}
