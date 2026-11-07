package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class FareIndexTest {

    private static FareIndex route(int... cents) {
        FareIndex index = new FareIndex();
        for (int c : cents) index.offer(c);
        return index;
    }

    @Test
    void findsTheFaresInsideARangeLowestFirst() {
        FareIndex index = route(18_900, 4_450, 12_500, 7_990, 25_000, 9_900);

        assertThat(index.between(5_000, 15_000)).containsExactly(7_990, 9_900, 12_500);
        assertThat(index.under(10_000)).containsExactly(4_450, 7_990, 9_900);
    }

    @Test
    void anEmptyRangeGivesNoFares() {
        FareIndex index = route(18_900, 4_450, 12_500);

        assertThat(index.between(13_000, 18_000)).isEmpty();
        assertThat(index.under(4_000)).isEmpty();
    }

    @Test
    void aFareExactlyAtTheTopOfTheRangeIsIncluded() {
        FareIndex index = route(6_400, 8_800, 11_000, 14_750);

        assertThat(index.between(6_400, 11_000)).containsExactly(6_400, 8_800, 11_000);
    }

    @Test
    void aSearchResultStaysAsItWasWhenTheSearchRan() {
        FareIndex index = route(3_300, 5_600, 7_700, 9_100);
        var result = index.between(3_000, 8_000);
        var cheap = index.under(8_000);

        index.offer(6_000);
        index.withdraw(5_600);

        assertThat(result).containsExactly(3_300, 5_600, 7_700);
        assertThat(cheap).containsExactly(3_300, 5_600, 7_700);
    }

    @Test
    void aFareExactlyAtTheLimitIsNotUnderIt() {
        FareIndex index = route(2_500, 4_000, 6_100);

        assertThat(index.under(4_000)).containsExactly(2_500);
    }
}
