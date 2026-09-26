package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GradesTest {

    @Test
    void gradesScoresInsideEachBand() {
        assertThat(Grades.letter(95)).isEqualTo("A");
        assertThat(Grades.letter(55)).isEqualTo("F");
        assertThat(Grades.letter(0)).isEqualTo("F");
        assertThat(Grades.letter(100)).isEqualTo("A");
    }

    @Test
    void theFirstMatchingBranchWins() {
        assertThat(Grades.letter(85)).isEqualTo("B");
        assertThat(Grades.letter(75)).isEqualTo("C");
        assertThat(Grades.letter(65)).isEqualTo("D");
    }

    @Test
    void eachLowerBoundBelongsToItsBand() {
        assertThat(Grades.letter(90)).isEqualTo("A");
        assertThat(Grades.letter(80)).isEqualTo("B");
        assertThat(Grades.letter(70)).isEqualTo("C");
        assertThat(Grades.letter(60)).isEqualTo("D");
        assertThat(Grades.letter(89)).isEqualTo("B");
        assertThat(Grades.letter(59)).isEqualTo("F");
    }

    @Test
    void aScoreOutsideTheRangeIsInvalid() {
        assertThat(Grades.letter(-1)).isEqualTo("Invalid");
        assertThat(Grades.letter(101)).isEqualTo("Invalid");
        assertThat(Grades.letter(Integer.MIN_VALUE)).isEqualTo("Invalid");
    }
}
