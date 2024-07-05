package com.github.msorkhpar.claudejavatutor.records;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class EmployeeTest {

    @Test
    void testRecordCreation() {
        Employee employee = new Employee("John Doe", 1, LocalDate.of(2023, 1, 1));
        assertThat(employee.name()).isEqualTo("John Doe");
        assertThat(employee.id()).isEqualTo(1);
        assertThat(employee.hireDate()).isEqualTo(LocalDate.of(2023, 1, 1));
    }

    @Test
    void testEquality() {
        Employee emp1 = new Employee("Jane Doe", 2, LocalDate.of(2023, 2, 1));
        Employee emp2 = new Employee("Jane Doe", 2, LocalDate.of(2023, 2, 1));
        Employee emp3 = new Employee("John Doe", 3, LocalDate.of(2023, 3, 1));

        assertThat(emp1).isEqualTo(emp2);
        assertThat(emp1).isNotEqualTo(emp3);
    }

    @Test
    void testToString() {
        Employee employee = new Employee("Alice Smith", 4, LocalDate.of(2023, 4, 1));
        assertThat(employee.toString()).isEqualTo("Employee(name=Alice Smith, id=4, hired on 2023-04-01)");
    }

    @ParameterizedTest
    @CsvSource({
            "Bob Johnson, 5, 2023-05-01, true",
            "Charlie Brown, 6, 2022-01-01, false"
    })
    void testIsNewHire(String name, int id, LocalDate hireDate, boolean expectedIsNewHire) {
        LocalDate mockedDate=LocalDate.of(2023, 8, 22);
        Clock fixedClock = Clock.fixed(mockedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());

        try (MockedStatic<Clock> mockedClock = mockStatic(Clock.class)) {
            mockedClock.when(Clock::systemDefaultZone).thenReturn(fixedClock);

            Employee employee = new Employee(name, id, hireDate);
            assertThat(employee.isNewHire()).isEqualTo(expectedIsNewHire);
        }
    }

    @Test
    void testValidation() {
        assertThatThrownBy(() -> new Employee(null, 1, LocalDate.now()))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Name cannot be null");

        assertThatThrownBy(() -> new Employee("Test", 0, LocalDate.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID must be positive");

        assertThatThrownBy(() -> new Employee("Test", 1, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Hire date cannot be null");
    }
}