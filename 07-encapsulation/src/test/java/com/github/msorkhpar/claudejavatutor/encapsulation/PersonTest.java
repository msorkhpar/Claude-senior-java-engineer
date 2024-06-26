package com.github.msorkhpar.claudejavatutor.encapsulation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class PersonTest {

    private Person person;

    @BeforeEach
    void setUp() {
        person = new Person();
    }

    @Test
    void testSetAndGetName() {
        person.setName("John Doe");
        assertThat(person.getName()).isEqualTo("John Doe");
    }

    @Test
    void testSetNameTrimsWhitespace() {
        person.setName("  Jane Doe  ");
        assertThat(person.getName()).isEqualTo("Jane Doe");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "  "})
    void testSetNameWithInvalidInput(String invalidName) {
        assertThatThrownBy(() -> person.setName(invalidName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name cannot be null or empty");
    }

    @Test
    void testSetAndGetAge() {
        person.setAge(30);
        assertThat(person.getAge()).isEqualTo(30);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 151})
    void testSetAgeWithInvalidInput(int invalidAge) {
        assertThatThrownBy(() -> person.setAge(invalidAge))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Age must be between 0 and 150");
    }

    @Test
    void testAddAndGetHobbies() {
        person.addHobby("Reading");
        person.addHobby("Swimming");
        assertThat(person.getHobbies()).containsExactly("Reading", "Swimming");
    }

    @Test
    void testGetHobbiesReturnsUnmodifiableList() {
        person.addHobby("Cooking");
        List<String> hobbies = person.getHobbies();
        assertThatThrownBy(() -> hobbies.add("Singing"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "  "})
    void testAddHobbyWithInvalidInput(String invalidHobby) {
        assertThatThrownBy(() -> person.addHobby(invalidHobby))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Hobby cannot be null or empty");
    }

    @Test
    void testAddHobbyTrimsWhitespace() {
        person.addHobby("  Gardening  ");
        assertThat(person.getHobbies()).containsExactly("Gardening");
    }
}