package practice;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TextProcessorTest {

    @Test
    void appliesTheStrategyAndSwitchesToALambda() {
        TextProcessor processor = new TextProcessor(TextProcessor.UPPER_CASE);
        assertThat(processor.process("hello")).isEqualTo("HELLO");

        processor.setStrategy(TextProcessor.LOWER_CASE);
        assertThat(processor.process("HeLLo")).isEqualTo("hello");
        processor.setStrategy(TextProcessor.REVERSE);
        assertThat(processor.process("hello")).isEqualTo("olleh");
        processor.setStrategy(TextProcessor.TRIM_AND_UPPER);
        assertThat(processor.process("  hello  ")).isEqualTo("HELLO");
        processor.setStrategy(TextProcessor.REMOVE_WHITESPACE);
        assertThat(processor.process("h e l l o")).isEqualTo("hello");
        processor.setStrategy(s -> s.replaceAll("\\s+", "-"));
        assertThat(processor.process("hello world")).isEqualTo("hello-world");
        assertThat(processor.process("")).isEmpty();
    }

    @Test
    void removeWhitespaceRemovesEveryKindOfWhitespace() {
        TextProcessor processor = new TextProcessor(TextProcessor.REMOVE_WHITESPACE);

        assertThat(processor.process("h e\tl\nlo   world")).isEqualTo("helloworld");
    }

    @Test
    void caseChangesDoNotDependOnTheDefaultLocale() {
        Locale before = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));

            assertThat(new TextProcessor(TextProcessor.UPPER_CASE).process("title")).isEqualTo("TITLE");
            assertThat(new TextProcessor(TextProcessor.TRIM_AND_UPPER).process(" title ")).isEqualTo("TITLE");
            assertThat(new TextProcessor(TextProcessor.LOWER_CASE).process("TITLE")).isEqualTo("title");
        } finally {
            Locale.setDefault(before);
        }
    }

    @Test
    void nullTextIsRefused() {
        TextProcessor processor = new TextProcessor(s -> s == null ? "was null" : s);

        assertThatThrownBy(() -> processor.process(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aNullStrategyIsRefused() {
        assertThatThrownBy(() -> new TextProcessor(null)).isInstanceOf(IllegalArgumentException.class);
        TextProcessor processor = new TextProcessor(TextProcessor.UPPER_CASE);
        assertThatThrownBy(() -> processor.setStrategy(null)).isInstanceOf(IllegalArgumentException.class);
        assertThat(processor.process("ok")).isEqualTo("OK");
    }
}
