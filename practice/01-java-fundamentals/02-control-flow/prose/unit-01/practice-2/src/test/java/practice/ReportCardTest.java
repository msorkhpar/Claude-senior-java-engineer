package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReportCardTest {

    @Test
    void printsTheLinesForEachGrade() {
        assertThat(ReportCard.lines(95)).startsWith("A");
        assertThat(ReportCard.lines(85)).startsWith("B");
        assertThat(ReportCard.lines(75)).startsWith("C");
        assertThat(ReportCard.lines(40)).startsWith("Needs improvement");
    }

    @Test
    void praiseBelongsOnlyToAnA() {
        assertThat(ReportCard.lines(95)).contains("Excellent!");
        assertThat(ReportCard.lines(85)).doesNotContain("Excellent!");
        assertThat(ReportCard.lines(10)).doesNotContain("Excellent!");
    }

    @Test
    void aHighScorePrintsOneLetterOnly() {
        assertThat(ReportCard.lines(100)).containsExactly("A", "Excellent!");
        assertThat(ReportCard.lines(90)).containsExactly("A", "Excellent!");
        assertThat(ReportCard.lines(95)).containsExactly("A", "Excellent!");
    }
}
