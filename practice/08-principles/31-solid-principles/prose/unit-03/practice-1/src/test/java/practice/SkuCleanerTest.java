package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkuCleanerTest {

    private static final List<String> CLEAN = List.of("BX-310", "KQ-7", "LM-0042");

    private static String[] feed() {
        return new String[] {" kq-7", "LM-0042", "bx-310 ", "   ", "KQ-7", "lm-0042"};
    }

    @Test
    void cleansAFeedTheParserBuiltAsAnArrayList() {
        List<String> skus = new ArrayList<>(Arrays.asList(feed()));

        assertThat(SkuCleaner.clean(skus)).containsExactlyElementsOf(CLEAN);
    }

    @Test
    void cleansAFeedTheParserReturnedFromListOf() {
        List<String> skus = List.of(feed());

        assertThat(SkuCleaner.clean(skus)).containsExactlyElementsOf(CLEAN);
    }

    @Test
    void leavesTheImportersOwnListAsItWas() {
        List<String> skus = new ArrayList<>(Arrays.asList(feed()));

        SkuCleaner.clean(skus);

        assertThat(skus).containsExactly(feed());
    }

    @Test
    void cleansAFeedTheParserWrappedWithArraysAsList() {
        List<String> skus = Arrays.asList(feed());

        assertThat(SkuCleaner.clean(skus)).containsExactlyElementsOf(CLEAN);
    }
}
