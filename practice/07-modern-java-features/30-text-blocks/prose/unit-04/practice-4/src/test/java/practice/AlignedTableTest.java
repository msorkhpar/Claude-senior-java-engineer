package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AlignedTableTest {

    private static final String FENCE = "\\s";

    @Test
    void padsEveryCellAndFencesTheEnd() {
        assertThat(AlignedTable.lines(List.of(List.of("Name", "Age"), List.of("Bob", "7"), List.of("Ann", "30"))))
                .isEqualTo(String.join("\n", "Name Age", "Bob  7 " + FENCE, "Ann  30" + FENCE));
        assertThat(AlignedTable.lines(List.of(List.of("id", "x"), List.of("1", "y"))))
                .isEqualTo(String.join("\n", "id x", "1  y"));
    }

    @Test
    void theWidestCellInAnyRowSetsTheWidth() {
        assertThat(AlignedTable.lines(List.of(List.of("k", "v"), List.of("key", "value"))))
                .isEqualTo(String.join("\n", "k   v   " + FENCE, "key value"));
    }

    @Test
    void aShortLastRowIsFencedToo() {
        assertThat(AlignedTable.lines(List.of(List.of("Name"), List.of("Alice"), List.of("Bob"))))
                .isEqualTo(String.join("\n", "Name" + FENCE, "Alice", "Bob " + FENCE));
    }

    @Test
    void anEmptyCellIsAllPadding() {
        assertThat(AlignedTable.lines(List.of(List.of("a", ""), List.of("bb", "cc"))))
                .isEqualTo(String.join("\n", "a  " + " " + FENCE, "bb cc"));
    }
}
