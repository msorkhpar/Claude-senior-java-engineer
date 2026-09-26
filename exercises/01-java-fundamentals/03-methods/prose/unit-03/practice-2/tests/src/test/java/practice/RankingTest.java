package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class RankingTest {

    @Test
    void ranksHighestFirst() {
        assertThat(Ranking.ranked(new int[]{70, 95, 80})).containsExactly(95, 80, 70);
        assertThat(Ranking.ranked(new int[]{1, 1, 2})).containsExactly(2, 1, 1);
        assertThat(Ranking.ranked(new int[]{})).isEmpty();
    }

    @Test
    void theCallersArrayIsUntouched() {
        int[] scores = {70, 95, 80};
        Ranking.ranked(scores);
        assertThat(scores).containsExactly(70, 95, 80);
    }

    @Test
    void theResultIsANewArray() {
        int[] scores = {3, 2, 1};
        assertThat(Ranking.ranked(scores)).isNotSameAs(scores).containsExactly(3, 2, 1);
    }
}
