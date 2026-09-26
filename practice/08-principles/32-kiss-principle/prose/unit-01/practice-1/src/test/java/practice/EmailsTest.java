package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class EmailsTest {

    @Test
    void acceptsAWellFormedAddress() {
        assertThat(Emails.isValidEmail("user@example.com")).isTrue();
        assertThat(Emails.isValidEmail("a@b")).isTrue();
        assertThat(Emails.isValidEmail("userexample.com")).isFalse();
        // only the first @ is looked at, and any characters may surround it
        assertThat(Emails.isValidEmail("a@b@c")).isTrue();
        assertThat(Emails.isValidEmail("a@@")).isTrue();
        assertThat(Emails.isValidEmail("a@b@")).isTrue();
        assertThat(Emails.isValidEmail("a b@c d")).isTrue();
    }

    @Test
    void nullOrEmptyIsInvalid() {
        assertThat(Emails.isValidEmail(null)).isFalse();
        assertThat(Emails.isValidEmail("")).isFalse();
    }

    @Test
    void aLocalPartIsRequired() {
        assertThat(Emails.isValidEmail("@example.com")).isFalse();
        assertThat(Emails.isValidEmail("@")).isFalse();
        assertThat(Emails.isValidEmail("@a@")).isFalse();
    }

    @Test
    void aDomainIsRequired() {
        assertThat(Emails.isValidEmail("user@")).isFalse();
    }
}
