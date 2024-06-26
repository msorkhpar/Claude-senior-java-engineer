package com.github.msorkhpar.claudejavatutor.classobj;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.assertj.core.api.Assertions.*;
import net.datafaker.Faker;

class PersonTest {

    private Person person;
    private Faker faker;

    @BeforeEach
    void setUp() {
        faker = new Faker();
        person = new Person(faker.name().fullName(), faker.number().numberBetween(18, 80));
    }

    @Test
    void testPersonCreation() {
        assertThat(person).isNotNull();
        assertThat(person.getName()).isNotBlank();
        assertThat(person.getAge()).isBetween(18, 80);
    }

    @Test
    void testSetName() {
        String newName = faker.name().fullName();
        person.setName(newName);
        assertThat(person.getName()).isEqualTo(newName);
    }

    @Test
    void testSetAge() {
        int newAge = faker.number().numberBetween(0, 120);
        person.setAge(newAge);
        assertThat(person.getAge()).isEqualTo(newAge);
    }

    @Test
    void testSetAgeWithNegativeValue() {
        assertThatThrownBy(() -> person.setAge(-1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Age cannot be negative");
    }

    @Test
    void testPersonCount() {
        int initialCount = Person.getPersonCount();
        new Person(faker.name().fullName(), faker.number().numberBetween(18, 80));
        assertThat(Person.getPersonCount()).isEqualTo(initialCount + 1);
    }

    @Test
    void testNestedAddressClass() {
        String street = faker.address().streetAddress();
        String city = faker.address().city();
        Person.Address address = new Person.Address(street, city);
        assertThat(address.getFullAddress()).isEqualTo(street + ", " + city);
    }
}