package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class GradePointsTest {

    @Test
    void givesThePointsOfEachGrade() {
        assertThat(GradePoints.points('A')).isEqualTo(4.0);
        assertThat(GradePoints.points('B')).isEqualTo(3.0);
        assertThat(GradePoints.points('C')).isEqualTo(2.0);
        assertThat(GradePoints.points('D')).isEqualTo(1.0);
        assertThat(GradePoints.points('F')).isEqualTo(0.0);
    }

    @Test
    void lowerCaseLettersCount() {
        assertThat(GradePoints.points('a')).isEqualTo(4.0);
        assertThat(GradePoints.points('d')).isEqualTo(1.0);
        assertThat(GradePoints.points('f')).isEqualTo(0.0);
    }

    @Test
    void otherCharactersAreRefused() {
        assertThatThrownBy(() -> GradePoints.points('E')).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> GradePoints.points('?')).isInstanceOf(IllegalArgumentException.class);
    }
}
