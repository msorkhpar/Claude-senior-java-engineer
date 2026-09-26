package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RosterTest {

    @Test
    void oneDepartmentRanksBySalaryDescending() {
        assertThat(Roster.ranked(List.of(
                new Employee("Eve", "QA", 90_000.0),
                new Employee("Fay", "QA", 90_000.5)))).containsExactly("Fay", "Eve");
        List<Employee> staff = List.of(
                new Employee("Bob", "Engineering", 70_000),
                new Employee("Alice", "Engineering", 95_000),
                new Employee("Charlie", "Engineering", 88_000));
        assertThat(Roster.ranked(staff)).containsExactly("Alice", "Charlie", "Bob");
        assertThat(Roster.ranked(List.of())).isEmpty();
    }

    @Test
    void departmentsStayAscendingWhileSalariesDescend() {
        assertThat(Roster.ranked(List.of(
                new Employee("Ann", "eng", 50_000),
                new Employee("Bo", "Ops", 50_000)))).containsExactly("Bo", "Ann");
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
        assertThat(Roster.ranked(List.of(
                new Employee("bea", "QA", 60_000),
                new Employee("Cal", "QA", 60_000)))).containsExactly("Cal", "bea");
        List<Employee> staff = List.of(
                new Employee("Eve", "Engineering", 95_000),
                new Employee("Alice", "Engineering", 95_000));
        assertThat(Roster.ranked(staff)).containsExactly("Alice", "Eve");
    }
}
