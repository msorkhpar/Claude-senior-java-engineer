package practice;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.*;

class DataProcessorTest {

    @Test
    void bothProcessorsShareThePipeline() throws Exception {
        assertThat(new DataProcessor.UpperCase().process(Arrays.asList("hello", "", null, "  ", "world")))
                .containsExactly("HELLO", "WORLD");
        assertThat(new DataProcessor.Doubler().process(Arrays.asList(1, -2, 0, 3, null, 200)))
                .containsExactly(2, 6, 400);
        assertThat(new DataProcessor.UpperCase().process(List.of())).isEmpty();
        assertThat(new DataProcessor.UpperCase().process(List.of(" hi ", "a b"))).containsExactly(" HI ", "A B");
        assertThat(new DataProcessor.Doubler().process(List.of(1000, 1000, 7))).containsExactly(2000, 2000, 14);
        assertThatThrownBy(() -> new DataProcessor.Doubler().process(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void aNewProcessorFillsInOnlyItsSteps() throws Exception {
        DataProcessor<String, Integer> tagLengths = new DataProcessor<>() {
            @Override
            protected boolean isValid(String item) {
                return item.startsWith("#");
            }

            @Override
            protected Integer transform(String item) {
                return item.length() - 1;
            }
        };
        assertThat(tagLengths.process(List.of("#java", "plain", "#dry"))).containsExactly(4, 3);
        DataProcessor<String, String> everything = new DataProcessor<>() {
            @Override
            protected boolean isValid(String item) {
                return true;
            }

            @Override
            protected String transform(String item) {
                return String.valueOf(item);
            }
        };
        assertThat(everything.process(Arrays.asList(null, "a"))).containsExactly("null", "a");
        assertThat(Modifier.isFinal(DataProcessor.class.getMethod("process", List.class).getModifiers()))
                .as("process is final").isTrue();
    }

    @Test
    void upperCaseIsTheSameInEveryLocale() throws Exception {
        Locale saved = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertThat(new DataProcessor.UpperCase().process(List.of("title", "index"))).containsExactly("TITLE", "INDEX");
        } finally {
            Locale.setDefault(saved);
        }
    }
}
