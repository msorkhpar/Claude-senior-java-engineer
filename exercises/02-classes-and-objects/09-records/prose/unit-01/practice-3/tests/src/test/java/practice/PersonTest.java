package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PersonTest {

    @Test
    void aPersonDescribesItself() {
        Person alice = new Person("Alice", 30);
        assertThat(alice.isAdult()).isTrue();
        assertThat(alice.print()).isEqualTo("Person: Alice, 30 years old");
        Printable printable = alice;
        assertThat(printable.print()).isEqualTo("Person: Alice, 30 years old");
        Person frank = Person.createAdult("Frank");
        assertThat(frank.name()).isEqualTo("Frank");
        assertThat(frank.age()).isEqualTo(18);
        assertThat(new Person("Tim", 12).isAdult()).isFalse();
    }

    @Test
    void eighteenIsAlreadyAdult() {
        assertThat(new Person("Nora", 18).isAdult()).isTrue();
        assertThat(Person.createAdult("Frank").isAdult()).isTrue();
        assertThat(new Person("Tim", 17).isAdult()).isFalse();
    }

    @Test
    void toStringReadsAsASentence() {
        assertThat(new Person("Bob", 25)).hasToString("Person named Bob is 25 years old");
    }

    @Test
    void equalityStillComparesComponents() {
        Person a = Person.createAdult("Charlie");
        assertThat(a).isEqualTo(new Person("Charlie", 18));
        assertThat(a.hashCode()).isEqualTo(new Person("Charlie", 18).hashCode());
        assertThat(a).isNotEqualTo(new Person("Charlie", 41));
    }
}
