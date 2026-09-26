package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersonTest {

    @Test
    void aValidPersonKeepsItsComponents() {
        Person person = new Person("Alice", 30);
        assertThat(person.name()).isEqualTo("Alice");
        assertThat(person.age()).isEqualTo(30);
        assertThat(person).hasToString("Person[name=Alice, age=30]");
        assertThat(person).isEqualTo(new Person("Alice", 30));
    }

    @Test
    void aNullNameIsRefused() {
        assertThatThrownBy(() -> new Person(null, 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name cannot be null or empty");
    }

    @Test
    void anEmptyNameIsRefused() {
        assertThatThrownBy(() -> new Person("", 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name cannot be null or empty");
    }

    @Test
    void aNegativeAgeIsRefused() {
        assertThatThrownBy(() -> new Person("Eve", -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Age cannot be negative");
    }

    @Test
    void ageZeroIsAllowed() {
        Person baby = new Person("Baby", 0);
        assertThat(baby.age()).isZero();
    }
}
