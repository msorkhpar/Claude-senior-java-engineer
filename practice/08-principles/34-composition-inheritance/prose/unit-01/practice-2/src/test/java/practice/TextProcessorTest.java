package practice;

import org.junit.jupiter.api.Test;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.*;

class TextProcessorTest {

    @Test
    void trimsThenUpperCases() {
        TextProcessor p = new TextProcessor();
        assertThat(p.addTransformation(String::trim)).isSameAs(p);
        assertThat(p.addTransformation(String::toUpperCase)).isSameAs(p);
        assertThat(p.process("  hello  ")).isEqualTo("HELLO");
        assertThat(p.addTransformation(s -> s)).isSameAs(p);
        assertThat(p.process("  hello  ")).isEqualTo("HELLO");
    }

    @Test
    void transformationsRunInTheOrderAdded() {
        TextProcessor p = new TextProcessor()
                .addTransformation(String::trim)
                .addTransformation(String::toUpperCase)
                .addTransformation(s -> s.replace(" ", "_"));
        assertThat(p.process("  hello world  ")).isEqualTo("HELLO_WORLD");
        Function<String, String> bang = s -> s + "!";
        TextProcessor twice = new TextProcessor().addTransformation(bang).addTransformation(String::trim).addTransformation(bang);
        assertThat(twice.process(" hi ")).as("one transformation added twice runs twice").isEqualTo("hi !!");
    }

    @Test
    void aProcessorWithNoTransformationsReturnsItsInput() {
        assertThat(new TextProcessor().process("as it is")).isEqualTo("as it is");
        assertThat(new TextProcessor().process("")).isEqualTo("");
    }

    @Test
    void aTransformationAddedLaterIsUsedNextTime() {
        TextProcessor p = new TextProcessor().addTransformation(String::trim);
        assertThat(p.process("  hi  ")).isEqualTo("hi");
        p.addTransformation(s -> s + "!");
        assertThat(p.process("  hi  ")).isEqualTo("hi!");
    }
}
