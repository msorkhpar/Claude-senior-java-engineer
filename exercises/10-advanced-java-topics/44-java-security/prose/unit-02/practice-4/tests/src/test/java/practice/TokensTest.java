package practice;

import java.util.BitSet;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokensTest {

    /** A token that records which of its characters were read. */
    static final class Watched implements CharSequence {
        private final String text;
        final BitSet read = new BitSet();

        Watched(String text) {
            this.text = text;
        }

        @Override
        public int length() {
            return text.length();
        }

        @Override
        public char charAt(int index) {
            read.set(index);
            return text.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            read.set(start, end);
            return text.subSequence(start, end);
        }

        @Override
        public String toString() {
            read.set(0, text.length());
            return text;
        }
    }

    private static String token(String... parts) {
        return String.join("", parts);
    }

    @Test
    void matchesOnlyTheSameToken() {
        assertThat(Tokens.matches(token("tok-", "4f9a2c"), new Watched(token("tok-4f", "9a2c")))).isTrue();
        assertThat(Tokens.matches(token("tok-", "4f9a2c"), new Watched(token("tok-4f", "9a2d")))).isFalse();
    }

    @Test
    void everyCharacterIsReadWhateverTheFirstMismatch() {
        Watched early = new Watched("xok-4f9a2c");
        Watched middle = new Watched("tok-0f9a2c");

        assertThat(Tokens.matches("tok-4f9a2c", early)).isFalse();
        assertThat(Tokens.matches("tok-4f9a2c", middle)).isFalse();

        assertThat(early.read.cardinality()).isEqualTo(10);
        assertThat(middle.read.cardinality()).isEqualTo(10);
    }

    @Test
    void aDifferenceBeforeTheLastCharacterStillCounts() {
        assertThat(Tokens.matches("tok-4f9a2c", new Watched("xok-4f9a2c"))).isFalse();
        assertThat(Tokens.matches("tok-4f9a2c", new Watched("tok-4f9a3c"))).isFalse();
    }

    @Test
    void aPrefixOrALongerTokenDoesNotMatch() {
        assertThat(Tokens.matches("tok-4f9a2c", new Watched("tok-4f9a2"))).isFalse();
        assertThat(Tokens.matches("tok-4f9a2c", new Watched("tok-4f9a2c0"))).isFalse();
        assertThat(Tokens.matches("tok-4f9a2c", new Watched(""))).isFalse();
    }

    @Test
    void aMissingTokenNeverMatches() {
        assertThat(Tokens.matches("tok-4f9a2c", null)).isFalse();
    }
}
