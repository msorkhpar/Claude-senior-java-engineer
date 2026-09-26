package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SqlTextTest {

    @Test
    void joinsIdsAndNames() {
        assertThat(SqlText.inClause(List.of(1, 2, 3, 4))).isEqualTo("(1, 2, 3, 4)");
        assertThat(SqlText.inClause(List.of(7))).isEqualTo("(7)");
        assertThat(SqlText.inClause(List.of(42, -3))).isEqualTo("(42, -3)");
        assertThat(SqlText.nameList(List.of("Alice", "Bob", "Charlie"))).isEqualTo("[Alice, Bob, Charlie]");
        assertThat(SqlText.nameList(List.of())).isEqualTo("[]");
    }

    @Test
    void noIdsGiveEmptyParentheses() {
        assertThat(SqlText.inClause(List.of())).isEqualTo("()");
    }

    @Test
    void blankNamesAreLeftOut() {
        assertThat(SqlText.nameList(List.of(new String("Alice"), new String(""), new String("Bob"), new String("   "))))
                .isEqualTo("[Alice, Bob]");
    }

    @Test
    void spacesOnlyNamesAreBlank() {
        assertThat(SqlText.nameList(List.of(new String("Ann"), new String("  "), new String("Bo")))).isEqualTo("[Ann, Bo]");
    }
}
