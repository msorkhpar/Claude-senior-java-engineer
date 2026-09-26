package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersonTest {

    @Test
    void storesValidValues() {
        Person person = new Person();
        person.setName("John Doe");
        person.setAge(30);
        assertThat(person.getName()).isEqualTo("John Doe");
        assertThat(person.getAge()).isEqualTo(30);
    }

    @Test
    void trimsTheName() {
        Person person = new Person();
        person.setName("  Jane Doe  ");
        assertThat(person.getName()).isEqualTo("Jane Doe");
    }

    @Test
    void rejectsANullName() {
        Person person = new Person();
        assertThatThrownBy(() -> person.setName(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name cannot be null or empty");
    }

    @Test
    void rejectsABlankName() {
        Person person = new Person();
        person.setName("John Doe");
        assertThatThrownBy(() -> person.setName("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Name cannot be null or empty");
        assertThat(person.getName()).isEqualTo("John Doe");
    }

    @Test
    void acceptsBothAgeBoundsOnly() {
        Person person = new Person();
        person.setAge(0);
        assertThat(person.getAge()).isZero();
        person.setAge(150);
        assertThat(person.getAge()).isEqualTo(150);
        assertThatThrownBy(() -> person.setAge(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Age must be between 0 and 150");
        assertThatThrownBy(() -> person.setAge(151))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Age must be between 0 and 150");
        assertThat(person.getAge()).isEqualTo(150);
    }
}
