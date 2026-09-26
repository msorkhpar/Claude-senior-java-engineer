package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreReportTest {

    @Test
    void reportsCountTotalBestAndPartition() {
        assertThat(ScoreReport.report(List.of(60, 40, 90, 100)))
                .isEqualTo("count=4 total=290 best=100 passed=[60, 90, 100] failed=[40]");
        assertThat(ScoreReport.report(List.of(49, 50)))
                .isEqualTo("count=2 total=99 best=50 passed=[50] failed=[49]");
    }

    @Test
    void sidesKeepTheInputOrder() {
        assertThat(ScoreReport.report(List.of(300, 49, 50, 1000, 7)))
                .isEqualTo("count=5 total=1406 best=1000 passed=[300, 50, 1000] failed=[49, 7]");
        assertThat(ScoreReport.report(List.of(70, 20, 55)))
                .isEqualTo("count=3 total=145 best=70 passed=[70, 55] failed=[20]");
    }

    @Test
    void noScoresHaveNoBest() {
        assertThat(ScoreReport.report(List.of()))
                .isEqualTo("count=0 total=0 best=none passed=[] failed=[]");
        assertThat(ScoreReport.report(List.of(0)))
                .isEqualTo("count=1 total=0 best=0 passed=[] failed=[0]");
        assertThat(ScoreReport.report(List.of(-5, -9)))
                .isEqualTo("count=2 total=-14 best=-5 passed=[] failed=[-5, -9]");
    }

    @Test
    void anEmptySideStillShows() {
        assertThat(ScoreReport.report(List.of(10, 20)))
                .isEqualTo("count=2 total=30 best=20 passed=[] failed=[10, 20]");
        assertThat(ScoreReport.report(List.of(70, 80)))
                .isEqualTo("count=2 total=150 best=80 passed=[70, 80] failed=[]");
    }
}
