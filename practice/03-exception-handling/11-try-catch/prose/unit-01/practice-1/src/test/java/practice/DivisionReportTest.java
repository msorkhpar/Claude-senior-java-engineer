package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DivisionReportTest {

    @Test
    void reportsEveryQuotient() {
        assertThat(DivisionReport.report(new int[][] {{10, 2}, {7, 2}, {-7, 2}, {0, 5}}))
                .containsExactly("10 / 2 = 5", "7 / 2 = 3", "-7 / 2 = -3", "0 / 5 = 0");
        assertThat(DivisionReport.report(new int[][] {})).isEmpty();
    }

    @Test
    void aZeroDivisorIsReportedWithTheExceptionMessage() {
        assertThat(DivisionReport.report(new int[][] {{10, 0}}))
                .containsExactly("10 / 0: / by zero");
    }

    @Test
    void aFailureDoesNotStopThePairsAfterIt() {
        assertThat(DivisionReport.report(new int[][] {{8, 4}, {10, 0}, {9, 3}, {1, 0}}))
                .containsExactly("8 / 4 = 2", "10 / 0: / by zero", "9 / 3 = 3", "1 / 0: / by zero");
    }
}
