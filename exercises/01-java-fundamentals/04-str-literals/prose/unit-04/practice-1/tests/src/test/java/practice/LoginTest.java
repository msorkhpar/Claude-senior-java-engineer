package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class LoginTest {

    @Test
    void comparesLiterals() {
        assertThat(Login.accepts("s3cret", "s3cret")).isTrue();
        assertThat(Login.accepts("s3cret", "secret")).isFalse();
    }

    @Test
    void aTextBuiltAtRunTimeMatches() {
        assertThat(Login.accepts("s3cret", new String("s3cret"))).isTrue();
        String typed = new StringBuilder().append('s').append(3).append("cret").toString();
        assertThat(Login.accepts("s3cret", typed)).isTrue();
    }
}
