package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

class TopEarnersTest {

    private static final List<Employee> STAFF = List.of(
            new Employee("Ann", "Eng", 90_000, true),
            new Employee("Bo", "Ops", 70_000, true),
            new Employee("Cy", "Eng", 110_000, true));

    @Test
    void ranksActiveEmployeesAndAveragesDepartments() {
        assertThat(TopEarners.names(STAFF, 2)).containsExactly("Cy", "Ann");
        assertThat(TopEarners.names(STAFF, 10)).containsExactly("Cy", "Ann", "Bo");
        assertThat(TopEarners.averageSalaryByDepartment(STAFF))
                .containsOnly(entry("Eng", 100_000.0), entry("Ops", 70_000.0));
    }

    @Test
    void anInactiveTopEarnerIsNotNamed() {
        List<Employee> staff = List.of(
                new Employee("Ann", "Eng", 90_000, true),
                new Employee("Dee", "Eng", 200_000, false),
                new Employee("Bo", "Ops", 70_000, true));
        assertThat(TopEarners.names(staff, 2)).containsExactly("Ann", "Bo");
    }

    @Test
    void anInactiveEmployeeIsNotAveraged() {
        List<Employee> staff = List.of(
                new Employee("Ann", "Eng", 90_000, true),
                new Employee("Dee", "Eng", 200_000, false),
                new Employee("Eli", "Legal", 50_000, false));
        assertThat(TopEarners.averageSalaryByDepartment(staff)).containsOnly(entry("Eng", 90_000.0));
    }

    @Test
    void departmentsAreGroupedByEqualNames() {
        List<Employee> staff = List.of(
                new Employee("Ann", new String("Eng"), 90_000, true),
                new Employee("Cy", new String("Eng"), 110_000, true));
        assertThat(TopEarners.averageSalaryByDepartment(staff)).containsOnly(entry("Eng", 100_000.0));
    }
}
