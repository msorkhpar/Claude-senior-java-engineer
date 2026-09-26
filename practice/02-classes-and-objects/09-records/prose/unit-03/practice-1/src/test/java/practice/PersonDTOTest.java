package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PersonDTOTest {

    @Test
    void parsesAWellFormedLine() {
        PersonDTO john = PersonDTO.parse("John Doe,30,john@example.com");
        assertThat(john).isEqualTo(new PersonDTO("John Doe", 30, "john@example.com"));
        assertThat(john.isAdult()).isTrue();
        assertThatThrownBy(() -> PersonDTO.parse("John Doe,30,invalidemail"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid email format");
    }

    @Test
    void spacesAroundFieldsAreIgnored() {
        assertThat(PersonDTO.parse(" Jane Doe , 15 , jane@example.com "))
                .isEqualTo(new PersonDTO("Jane Doe", 15, "jane@example.com"));
    }

    @Test
    void aLineWithTheWrongFieldCountIsRefused() {
        assertThatThrownBy(() -> PersonDTO.parse("John Doe,30"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PersonDTO.parse("John Doe,30,john@example.com,admin"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anAgeThatIsNoNumberIsRefused() {
        assertThatThrownBy(() -> PersonDTO.parse("John Doe,thirty,john@example.com"))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }
}
