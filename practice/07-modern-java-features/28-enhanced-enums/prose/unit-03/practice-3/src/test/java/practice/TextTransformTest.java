package practice;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TextTransformTest {

    @Test
    void eachConstantTransformsText() {
        assertThat(TextTransform.UPPER_CASE.apply("Hello")).isEqualTo("HELLO");
        assertThat(TextTransform.LOWER_CASE.apply("Hello")).isEqualTo("hello");
        assertThat(TextTransform.TRIM.apply("  hi  ")).isEqualTo("hi");
        assertThat(TextTransform.REVERSE.apply("abc")).isEqualTo("cba");
        assertThat(TextTransform.CAPITALIZE.apply("hELLO")).isEqualTo("Hello");
        assertThat(TextTransform.SNAKE_CASE.apply(" Hello World ")).isEqualTo("hello_world");
        assertThat(TextTransform.applyAll("abc", TextTransform.REVERSE, TextTransform.UPPER_CASE)).isEqualTo("CBA");
    }

    @Test
    void chainAppliesLeftToRight() {
        assertThat(TextTransform.applyAll("  hello WORLD  ", TextTransform.TRIM, TextTransform.CAPITALIZE))
                .isEqualTo("Hello world");
    }

    @Test
    void capitalizeKeepsAnEmptyText() {
        assertThat(TextTransform.CAPITALIZE.apply("")).isEmpty();
    }

    @Test
    void snakeCaseJoinsRunsOfSpace() {
        assertThat(TextTransform.SNAKE_CASE.apply("  Hello   big\tWorld ")).isEqualTo("hello_big_world");
    }

    @Test
    void nullTextIsRefused() {
        for (TextTransform t : TextTransform.values()) {
            assertThatThrownBy(() -> t.apply(null)).as(t.name()).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void caseIgnoresTheDefaultLocale() {
        Locale saved = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertThat(TextTransform.UPPER_CASE.apply("title")).isEqualTo("TITLE");
            assertThat(TextTransform.CAPITALIZE.apply("iSTANBUL")).isEqualTo("Istanbul");
        } finally {
            Locale.setDefault(saved);
        }
        Locale.setDefault(Locale.forLanguageTag("tr-TR"));
        try {
            assertThat(TextTransform.LOWER_CASE.apply("TITLE")).isEqualTo("title");
            assertThat(TextTransform.SNAKE_CASE.apply("TITLE")).isEqualTo("title");
            assertThat(TextTransform.CAPITALIZE.apply("TITLE")).isEqualTo("Title");
        } finally {
            Locale.setDefault(saved);
        }
    }
}
