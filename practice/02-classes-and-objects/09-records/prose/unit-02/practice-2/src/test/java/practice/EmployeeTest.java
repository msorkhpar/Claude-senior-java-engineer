package practice;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmployeeTest {

    private static final LocalDate TODAY = LocalDate.of(2023, 8, 22);

    @Test
    void aValidEmployeeReadsWell() {
        Employee alice = new Employee("Alice Smith", 4, LocalDate.of(2023, 4, 1));
        assertThat(alice.name()).isEqualTo("Alice Smith");
        assertThat(alice.id()).isEqualTo(4);
        assertThat(alice.hireDate()).isEqualTo(LocalDate.of(2023, 4, 1));
        assertThat(alice.toString()).isEqualTo("Employee(name=Alice Smith, id=4, hired on 2023-04-01)");
        assertThat(alice).isEqualTo(new Employee("Alice Smith", 4, LocalDate.of(2023, 4, 1)));
        assertThat(new Employee("Bob Johnson", 5, LocalDate.of(2023, 5, 1)).isNewHire(TODAY)).isTrue();
        assertThat(new Employee("Charlie Brown", 6, LocalDate.of(2022, 1, 1)).isNewHire(TODAY)).isFalse();
    }

    @Test
    void missingValuesAreRefusedWithTheirOwnMessage() {
        assertThatThrownBy(() -> new Employee(null, 1, TODAY))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Name cannot be null");
        assertThatThrownBy(() -> new Employee("Test", 1, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Hire date cannot be null");
    }

    @Test
    void anIdMustBePositive() {
        assertThatThrownBy(() -> new Employee("Test", 0, TODAY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID must be positive");
        assertThatThrownBy(() -> new Employee("Test", -3, TODAY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID must be positive");
    }

    @Test
    void exactlySixMonthsIsNoLongerNew() {
        assertThat(new Employee("Dana", 7, LocalDate.of(2023, 2, 22)).isNewHire(TODAY)).isFalse();
        assertThat(new Employee("Eli", 8, LocalDate.of(2023, 2, 23)).isNewHire(TODAY)).isTrue();
    }
}
