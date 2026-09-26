package practice;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReportLinesTest {

    @Test
    void buildsHeaderItemsAndFooter() {
        assertThat(ReportLines.lines(List.of("item1", "item2"), true, true))
                .containsExactly("HEADER", "item1", "item2", "FOOTER");
        assertThat(ReportLines.lines(List.of("item1"), false, true)).containsExactly("item1", "FOOTER");
        assertThat(ReportLines.lines(List.of("item1"), true, false)).containsExactly("HEADER", "item1");
        assertThat(ReportLines.lines(List.of(), true, true)).containsExactly("HEADER", "FOOTER");
        assertThat(ReportLines.lines(List.of(), false, false)).isEmpty();
        assertThat(ReportLines.lines(List.of("item one", "item1", "item one"), false, false))
                .containsExactly("item one", "item1", "item one");
    }

    @Test
    void blankItemsAreSkipped() {
        assertThat(ReportLines.lines(List.of("item1", "   ", new String(""), "item2"), false, false)).containsExactly("item1", "item2");
    }

    @Test
    void itemsAreTrimmed() {
        assertThat(ReportLines.lines(List.of("  item1 ", "item2  "), true, false))
                .containsExactly("HEADER", "item1", "item2");
    }

    @Test
    void nullItemsAreSkipped() {
        assertThat(ReportLines.lines(Arrays.asList("item1", null, "item2"), false, true))
                .containsExactly("item1", "item2", "FOOTER");
    }
}
