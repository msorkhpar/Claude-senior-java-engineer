package com.github.msorkhpar.claudejavatutor.records;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

class PersonDTOTest {

    @Test
    void testValidPersonCreation() {
        PersonDTO person = new PersonDTO("John Doe", 30, "john@example.com");
        assertThat(person.name()).isEqualTo("John Doe");
        assertThat(person.age()).isEqualTo(30);
        assertThat(person.email()).isEqualTo("john@example.com");
    }

    @Test
    void testIsAdultMethod() {
        PersonDTO adult = new PersonDTO("John Doe", 30, "john@example.com");
        PersonDTO minor = new PersonDTO("Jane Doe", 15, "jane@example.com");
        assertThat(adult.isAdult()).isTrue();
        assertThat(minor.isAdult()).isFalse();
    }

    @ParameterizedTest
    @CsvSource({
            ", 30, john@example.com",
            "'', 30, john@example.com"
    })
    void testInvalidName(String name, int age, String email) {
        assertThatThrownBy(() -> new PersonDTO(name, age, email))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name cannot be null or empty");
    }

    @Test
    void testInvalidAge() {
        assertThatThrownBy(() -> new PersonDTO("John Doe", -1, "john@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Age cannot be negative");
    }

    @ParameterizedTest
    @CsvSource({
            "John Doe, 30, ",
            "John Doe, 30, invalidemail"
    })
    void testInvalidEmail(String name, int age, String email) {
        assertThatThrownBy(() -> new PersonDTO(name, age, email))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid email format");
    }
}