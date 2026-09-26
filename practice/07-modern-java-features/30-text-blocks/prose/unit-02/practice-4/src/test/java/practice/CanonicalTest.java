package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CanonicalTest {

    @Test
    void aLiteralIsAlreadyThePooledInstance() {
        assertThat(Canonical.of("Bye")).isSameAs("Bye");
        assertThat(Canonical.of("")).isSameAs("");
    }

    @Test
    void aStringBuiltAtRunTimeGetsThePooledInstance() {
        String built = new StringBuilder("Hello").append('\n').append("World").toString();
        assertThat(built).isNotSameAs(Canonical.GREETING);
        assertThat(Canonical.of(built)).isSameAs(Canonical.GREETING);
        assertThat(Canonical.of("%s%nWorld".formatted("Hello").replace(System.lineSeparator(), "\n")))
                .isSameAs(Canonical.GREETING);
    }

    @Test
    void anyTextGetsOneInstance() {
        String a = String.join("-", "order", Integer.toString(40_000 + 2));
        String b = new StringBuilder("order-").append(40_002).toString();
        assertThat(a).isNotSameAs(b);
        String first = Canonical.of(a);
        assertThat(first).isSameAs("order-40002");
        assertThat(Canonical.of(b)).isSameAs(first);
    }
}
