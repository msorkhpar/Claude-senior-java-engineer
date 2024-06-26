package com.github.msorkhpar.claudejavatutor.classobj;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class Person2Test {

    private LocalDate validDate;

    @BeforeEach
    void setUp() {
        validDate = LocalDate.of(1990, 1, 1);
    }

    @Test
    void testConstructorWithAllFields() {
        Person2 person = new Person2("John Doe", validDate, "john@example.com");
        assertThat(person.getName()).isEqualTo("John Doe");
        assertThat(person.getDateOfBirth()).isEqualTo(validDate);
        assertThat(person.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void testConstructorWithoutEmail() {
        Person2 person = new Person2("Jane Doe", validDate);
        assertThat(person.getName()).isEqualTo("Jane Doe");
        assertThat(person.getDateOfBirth()).isEqualTo(validDate);
        assertThat(person.getEmail()).isNull();
    }

    @Test
    void testCopyConstructor() {
        Person2 original = new Person2("Alice", validDate, "alice@example.com");
        Person2 copy = new Person2(original);
        assertThat(copy).usingRecursiveComparison().isEqualTo(original);
        assertThat(copy).isNotSameAs(original);
    }

    @ParameterizedTest
    @NullSource
    void testConstructorWithInvalidName(String invalidName) {
        assertThatThrownBy(() -> new Person2(invalidName, validDate))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Name cannot be null");
    }

    @Test
    void testConstructorWithNullDateOfBirth() {
        assertThatThrownBy(() -> new Person2("John Doe", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Date of birth cannot be null");
    }

    @Test
    void testSetEmail() {
        Person2 person = new Person2("Bob", validDate);
        person.setEmail("bob@example.com");
        assertThat(person.getEmail()).isEqualTo("bob@example.com");
    }
}