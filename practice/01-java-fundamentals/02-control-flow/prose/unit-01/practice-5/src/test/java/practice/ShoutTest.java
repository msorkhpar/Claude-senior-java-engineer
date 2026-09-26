package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShoutTest {

    @Test
    void shoutsLongTextAndKeepsShortText() {
        assertThat(Shout.shout("welcome")).isEqualTo("WELCOME");
        assertThat(Shout.shout("hi")).isEqualTo("hi");
    }

    @Test
    void fiveCharactersAreNotLong() {
        assertThat(Shout.shout("hello")).isEqualTo("hello");
        assertThat(Shout.shout("abcdef")).isEqualTo("ABCDEF");
    }

    @Test
    void nullIsNotAString() {
        assertThat(Shout.shout(null)).isEqualTo("");
    }

    @Test
    void otherTypesGiveNothing() {
        assertThat(Shout.shout(42)).isEqualTo("");
        assertThat(Shout.shout(new StringBuilder("welcome"))).isEqualTo("");
    }
}
