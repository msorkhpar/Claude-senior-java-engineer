package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RosterTest {

    @Test
    void oneDepartmentRanksBySalaryDescending() {
        List<Employee> staff = List.of(
                new Employee("Bob", "Engineering", 70_000),
                new Employee("Alice", "Engineering", 95_000),
                new Employee("Charlie", "Engineering", 88_000));
        assertThat(Roster.ranked(staff)).containsExactly("Alice", "Charlie", "Bob");
        assertThat(Roster.ranked(List.of())).isEmpty();
    }

    @Test
    void departmentsStayAscendingWhileSalariesDescend() {
        List<Employee> staff = List.of(
                new Employee("Alice", "Engineering", 95_000),
                new Employee("Bob", "Marketing", 75_000),
                new Employee("Charlie", "Engineering", 88_000),
                new Employee("Diana", "Marketing", 82_000),
                new Employee("Eve", "Engineering", 95_000));
        assertThat(Roster.ranked(staff)).containsExactly("Alice", "Eve", "Charlie", "Diana", "Bob");
    }

    @Test
    void equalSalariesFallBackToName() {
        List<Employee> staff = List.of(
                new Employee("Eve", "Engineering", 95_000),
                new Employee("Alice", "Engineering", 95_000));
        assertThat(Roster.ranked(staff)).containsExactly("Alice", "Eve");
    }
}
