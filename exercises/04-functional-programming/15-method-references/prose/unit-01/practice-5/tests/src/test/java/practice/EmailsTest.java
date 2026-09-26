package practice;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmailsTest {

    @Test
    void lowerCasesAndSortsTheEmails() {
        List<User> users = List.of(
                new User("Bob", "bob@example.org"),
                new User("Alice", "Alice@Example.com"));
        assertThat(Emails.normalised(users)).containsExactly("alice@example.com", "bob@example.org");
        assertThat(Emails.normalised(List.of())).isEmpty();
    }

    @Test
    void aNullUserIsSkipped() {
        List<User> users = Arrays.asList(
                new User("Alice", "alice@example.com"),
                null,
                new User("Charlie", "CHARLIE@example.com"));
        assertThat(Emails.normalised(users)).containsExactly("alice@example.com", "charlie@example.com");
    }

    @Test
    void aMissingEmailIsSkipped() {
        List<User> users = List.of(
                new User("Bob", null),
                new User("Alice", "alice@example.com"));
        assertThat(Emails.normalised(users)).containsExactly("alice@example.com");
    }

    @Test
    void emailsThatDifferOnlyInCaseAreOneEmail() {
        List<User> users = List.of(
                new User("Alice", "ALICE@example.com"),
                new User("Alice again", "alice@example.com"));
        assertThat(Emails.normalised(users)).containsExactly("alice@example.com");
    }

    @Test
    void equalEmailsHeldAsSeparateStringsAreOne() {
        List<User> users = List.of(
                new User("Bob", new String("bob@example.org")),
                new User("Bob again", new String("bob@example.org")));
        assertThat(Emails.normalised(users)).containsExactly("bob@example.org");
    }

    @Test
    void otherEmailsAreKeptAsGiven() {
        List<User> users = List.of(
                new User("Bob", " Bob@Example.org "),
                new User("Dee", ""),
                new User("Eli", "   "));
        assertThat(Emails.normalised(users)).containsExactly("", "   ", " bob@example.org ");
    }
}
