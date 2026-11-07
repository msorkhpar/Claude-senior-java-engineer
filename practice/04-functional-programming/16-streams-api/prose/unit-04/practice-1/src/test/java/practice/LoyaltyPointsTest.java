package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoyaltyPointsTest {

    private static List<LoyaltyPoints.Purchase> purchases(int n) {
        List<LoyaltyPoints.Purchase> out = new ArrayList<>();
        for (int i = 0; i < n; i++) out.add(new LoyaltyPoints.Purchase("r-" + i, (i * 7) % 50 + 1));
        return out;
    }

    private static List<Integer> redemptions(int n) {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < n; i++) out.add(i % 9 + 1);
        return out;
    }

    private static int sumOfPoints(List<LoyaltyPoints.Purchase> purchases) {
        int sum = 0;
        for (LoyaltyPoints.Purchase p : purchases) sum += p.points();
        return sum;
    }

    private static int sum(List<Integer> values) {
        int sum = 0;
        for (int v : values) sum += v;
        return sum;
    }

    @Test
    void shortHistoriesAddUp() {
        List<LoyaltyPoints.Purchase> three = List.of(new LoyaltyPoints.Purchase("r-a", 120),
                new LoyaltyPoints.Purchase("r-b", 45), new LoyaltyPoints.Purchase("r-c", 80));

        assertThat(LoyaltyPoints.earned(three, 300)).isEqualTo(545);
        assertThat(LoyaltyPoints.earned(List.of(), 300)).isEqualTo(300);
        assertThat(LoyaltyPoints.remaining(2_000, List.of(150, 600, 25))).isEqualTo(1_225);
        assertThat(LoyaltyPoints.remaining(2_000, List.of())).isEqualTo(2_000);
    }

    @Test
    void aLongHistoryGetsTheWelcomeBonusOnce() {
        List<LoyaltyPoints.Purchase> history = purchases(4_000);

        assertThat(LoyaltyPoints.earned(history, 300)).isEqualTo(300 + sumOfPoints(history));
    }

    @Test
    void aLongHistoryOfRedemptionsComesOffTheOpeningBalance() {
        List<Integer> history = redemptions(2_500);

        assertThat(LoyaltyPoints.remaining(100_000, history)).isEqualTo(100_000 - sum(history));
    }
}
