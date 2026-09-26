package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DeptReportTest {

    private static final List<DeptReport.Employee> STAFF = List.of(
            new DeptReport.Employee("Alice", "Engineering", 90000),
            new DeptReport.Employee("Bob", "Engineering", 85000),
            new DeptReport.Employee("Carol", "Marketing", 70000),
            new DeptReport.Employee("Dave", "Marketing", 72000),
            new DeptReport.Employee("Eve", "Engineering", 95000));

    @Test
    void summarisesEachDepartment() {
        assertThat(DeptReport.headcount(STAFF)).isEqualTo(Map.of("Engineering", 3L, "Marketing", 2L));
        assertThat(DeptReport.averageSalary(STAFF)).isEqualTo(Map.of("Engineering", 90000.0, "Marketing", 71000.0));
        assertThat(DeptReport.namesByDept(STAFF)).isEqualTo(Map.of(
                "Engineering", List.of("Alice", "Bob", "Eve"),
                "Marketing", List.of("Carol", "Dave")));
        assertThat(DeptReport.headcount(List.of())).isEmpty();
    }

    @Test
    void splitsStaffByPay() {
        assertThat(DeptReport.splitByPay(STAFF, 80000)).isEqualTo(Map.of(
                true, List.of("Alice", "Bob", "Eve"),
                false, List.of("Carol", "Dave")));
        assertThat(DeptReport.splitByPay(STAFF, 85000)).containsEntry(true, List.of("Alice", "Bob", "Eve"));
    }

    @Test
    void averageKeepsTheFraction() {
        List<DeptReport.Employee> ops = List.of(
                new DeptReport.Employee("Finn", "Ops", 100),
                new DeptReport.Employee("Gus", "Ops", 101));
        assertThat(DeptReport.averageSalary(ops)).isEqualTo(Map.of("Ops", 100.5));
    }

    @Test
    void bothSidesAreAlwaysPresent() {
        assertThat(DeptReport.splitByPay(STAFF, 1_000_000)).isEqualTo(Map.of(
                true, List.of(),
                false, List.of("Alice", "Bob", "Carol", "Dave", "Eve")));
    }

    @Test
    void departmentsMatchByValue() {
        List<DeptReport.Employee> staff = List.of(
                new DeptReport.Employee(new String("Hana"), new String("Sales"), 50000),
                new DeptReport.Employee(new String("Ivan"), new String("Sales"), 52000),
                new DeptReport.Employee(new String("Jin"), new String("Legal"), 60000));
        assertThat(DeptReport.headcount(staff)).isEqualTo(Map.of("Sales", 2L, "Legal", 1L));
    }
}
