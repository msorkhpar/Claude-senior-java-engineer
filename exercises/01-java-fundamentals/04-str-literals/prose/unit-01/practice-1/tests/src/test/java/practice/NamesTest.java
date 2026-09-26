package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class NamesTest {

    @Test
    void keepsTheText() {
        assertThat(Names.canonical("Hello")).isEqualTo("Hello");
        assertThat(Names.canonical(new String("Ada"))).isEqualTo("Ada");
    }

    @Test
    void equalNamesBecomeOneObject() {
        String built = new String("Hello");
        assertThat(Names.canonical(built)).isSameAs("Hello");
        String a = new StringBuilder("gr").append("ace").toString();
        String b = new String("grace");
        assertThat(Names.canonical(a)).isSameAs(Names.canonical(b));
    }

    @Test
    void nullStaysNull() {
        assertThat(Names.canonical(null)).isNull();
    }
}
