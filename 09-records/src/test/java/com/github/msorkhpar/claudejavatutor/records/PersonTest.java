package com.github.msorkhpar.claudejavatutor.records;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersonTest {

    @Test
    void testValidPerson() {
        Person person = new Person("Alice", 30);
        assertThat(person.name()).isEqualTo("Alice");
        assertThat(person.age()).isEqualTo(30);
    }

    @Test
    void testToString() {
        Person person = new Person("Bob", 25);
        assertThat(person.toString()).isEqualTo("Person[name=Bob, age=25]");
    }

    @Test
    void testEquality() {
        Person person1 = new Person("Charlie", 40);
        Person person2 = new Person("Charlie", 40);
        Person person3 = new Person("David", 40);

        assertThat(person1).isEqualTo(person2);
        assertThat(person1).isNotEqualTo(person3);
    }

    @Test
    void testInvalidName() {
        assertThatThrownBy(() -> new Person(null, 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name cannot be null or empty");

        assertThatThrownBy(() -> new Person("", 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name cannot be null or empty");
    }

    @Test
    void testInvalidAge() {
        assertThatThrownBy(() -> new Person("Eve", -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Age cannot be negative");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 17, 18, 30, 100})
    void testIsAdult(int age) {
        Person person = new Person("Test", age);
        assertThat(person.isAdult()).isEqualTo(age >= 18);
    }

    @Test
    void testCreateAdult() {
        Person adult = Person.createAdult("Frank");
        assertThat(adult.name()).isEqualTo("Frank");
        assertThat(adult.age()).isEqualTo(18);
        assertThat(adult.isAdult()).isTrue();
    }
}