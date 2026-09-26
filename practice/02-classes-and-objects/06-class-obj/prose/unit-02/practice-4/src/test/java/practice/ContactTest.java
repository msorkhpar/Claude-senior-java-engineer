package practice;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ContactTest {

    private static final LocalDate BORN = LocalDate.of(1990, 1, 1);

    @Test
    void aCopyIsANewContactWithTheSameState() {
        Contact ada = new Contact("Ada", BORN, "ada@example.com", List.of("555-0100"));
        Contact copy = new Contact(ada);
        assertThat(copy).isNotSameAs(ada);
        assertThat(copy.getName()).isEqualTo("Ada");
        assertThat(copy.getDateOfBirth()).isEqualTo(BORN);
        assertThat(copy.getEmail()).isEqualTo("ada@example.com");
        assertThat(copy.getPhones()).containsExactly("555-0100");
    }

    @Test
    void aCopyOwnsItsPhoneList() {
        Contact ada = new Contact("Ada", BORN, "ada@example.com", List.of("555-0100"));
        Contact copy = new Contact(ada);
        ada.addPhone("555-0199");
        assertThat(ada.getPhones()).containsExactly("555-0100", "555-0199");
        assertThat(copy.getPhones()).containsExactly("555-0100");
    }

    @Test
    void theCallersListIsNotShared() {
        List<String> phones = new ArrayList<>(List.of("555-0100"));
        Contact ada = new Contact("Ada", BORN, "ada@example.com", phones);
        phones.add("555-0199");
        phones.set(0, "555-0000");
        assertThat(ada.getPhones()).containsExactly("555-0100");
    }

    @Test
    void theShortConstructorLeavesNoEmailAndNoPhones() {
        Contact bob = new Contact("Bob", BORN);
        assertThat(bob.getName()).isEqualTo("Bob");
        assertThat(bob.getEmail()).isNull();
        assertThat(bob.getPhones()).isEmpty();
        bob.addPhone("555-0142");
        assertThat(bob.getPhones()).containsExactly("555-0142");
    }

    @Test
    void aMissingNameOrDateIsRefusedByName() {
        assertThatThrownBy(() -> new Contact(null, BORN))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Name cannot be null");
        assertThatThrownBy(() -> new Contact("Bob", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Date of birth cannot be null");
    }
}
