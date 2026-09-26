package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WalkTest {

    @Test
    void findsTheRevisitedCell() {
        assertThat(Walk.firstRevisit("RRL")).isEqualTo(new Walk.Cell(0, 1));
        assertThat(Walk.firstRevisit("DDRUL")).isEqualTo(new Walk.Cell(1, 0));
    }

    @Test
    void theStartCellCountsAsVisited() {
        assertThat(Walk.firstRevisit("UD")).isEqualTo(new Walk.Cell(0, 0));
        assertThat(Walk.firstRevisit("RDLU")).isEqualTo(new Walk.Cell(0, 0));
    }

    @Test
    void stopsAtTheFirstRevisit() {
        assertThat(Walk.firstRevisit("RRLLRR")).isEqualTo(new Walk.Cell(0, 1));
    }

    @Test
    void aWalkWithNoRevisitIsNull() {
        assertThat(Walk.firstRevisit("URDR")).isNull();
        assertThat(Walk.firstRevisit("")).isNull();
    }
}
