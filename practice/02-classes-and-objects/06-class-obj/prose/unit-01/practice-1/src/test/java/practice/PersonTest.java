package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersonTest {

    @Test
    void createsAndIntroduces() {
        Person ada = new Person("Ada", 36);
        assertThat(ada.getName()).isEqualTo("Ada");
        assertThat(ada.getAge()).isEqualTo(36);
        assertThat(ada.introduce()).isEqualTo("Hello, my name is Ada and I am 36 years old.");
        ada.setAge(37);
        assertThat(ada.getAge()).isEqualTo(37);
        assertThat(ada.introduce()).isEqualTo("Hello, my name is Ada and I am 37 years old.");
    }

    @Test
    void aNegativeAgeIsRefused() {
        Person bob = new Person("Bob", 30);
        assertThatThrownBy(() -> bob.setAge(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Age cannot be negative");
    }

    @Test
    void aRefusedAgeKeepsTheOldAge() {
        Person bob = new Person("Bob", 30);
        try {
            bob.setAge(-5);
        } catch (IllegalArgumentException expected) {
            // the refusal is what the rule asks for
        }
        assertThat(bob.getAge()).isEqualTo(30);
    }

    @Test
    void theConstructorRefusesANegativeAge() {
        Person anyone = new Person("Anyone", 1);
        assertThat(anyone.getAge()).isEqualTo(1);
        assertThatThrownBy(() -> new Person("Eve", -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anAddressJoinsStreetAndCity() {
        Person.Address address = new Person.Address("1 Main St", "Springfield");
        assertThat(address.getFullAddress()).isEqualTo("1 Main St, Springfield");
    }
}
