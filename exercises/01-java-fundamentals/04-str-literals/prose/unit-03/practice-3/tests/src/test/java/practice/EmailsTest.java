package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class EmailsTest {

    @Test
    void findsTheUser() {
        assertThat(Emails.user("ada@example.com")).isEqualTo("ada");
        assertThat(Emails.user("grace.hopper@navy.mil")).isEqualTo("grace.hopper");
        assertThat(Emails.user("ada")).isEqualTo("ada");
    }

    @Test
    void theDomainStartsAfterTheAt() {
        assertThat(Emails.domain("ada@example.com")).isEqualTo("example.com");
        assertThat(Emails.domain("x@y")).isEqualTo("y");
    }

    @Test
    void noAtMeansNoDomain() {
        assertThat(Emails.domain("ada")).isEmpty();
    }
}
