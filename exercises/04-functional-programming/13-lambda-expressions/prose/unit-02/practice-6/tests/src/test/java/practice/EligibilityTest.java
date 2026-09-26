package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EligibilityTest {

    @Test
    void keepsActiveVerifiedAdults() {
        List<Eligibility.User> users = List.of(
                new Eligibility.User("David", 22, true, true),
                new Eligibility.User("Bob", 17, true, true),
                new Eligibility.User("Charlie", 30, false, true),
                new Eligibility.User("Alice", 25, true, true));
        assertThat(Eligibility.eligibleNames(users)).containsExactly("Alice", "David");
        assertThat(Eligibility.isEligible().test(new Eligibility.User("Alice", 25, true, true))).isTrue();
        assertThat(Eligibility.isEligible().test(new Eligibility.User("Bob", 17, true, true))).isFalse();
        assertThat(Eligibility.eligibleNames(List.of())).isEmpty();
    }

    @Test
    void eighteenIsAnAdult() {
        assertThat(Eligibility.isEligible().test(new Eligibility.User("Eve", 18, true, true))).isTrue();
        assertThat(Eligibility.eligibleNames(List.of(new Eligibility.User("Eve", 18, true, true))))
                .containsExactly("Eve");
    }

    @Test
    void unverifiedEmailIsExcluded() {
        Eligibility.User frank = new Eligibility.User("Frank", 40, true, false);
        assertThat(Eligibility.isEligible().test(frank)).isFalse();
        assertThat(Eligibility.eligibleNames(List.of(frank))).isEmpty();
    }
}
