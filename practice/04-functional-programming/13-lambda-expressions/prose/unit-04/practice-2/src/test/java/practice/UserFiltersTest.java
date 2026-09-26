package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserFiltersTest {

    @Test
    void keepsEligibleUserNames() {
        List<User> users = List.of(
                new User("Alice", 30, true, "alice@example.com", true),
                new User("Bob", 16, true, null, false),
                new User("Carol", 40, false, "carol@example.com", false),
                new User("Dan", 52, true, "dan@example.com", true));

        assertThat(UserFilters.eligibleNames(users)).containsExactly("Alice", "Dan");
        assertThat(UserFilters.IS_ADULT.test(users.get(0))).isTrue();
        assertThat(UserFilters.IS_ACTIVE.test(users.get(2))).isFalse();
    }

    @Test
    void eighteenIsAdult() {
        User eighteen = new User("Eve", 18, true, "eve@example.com", true);

        assertThat(UserFilters.IS_ADULT.test(eighteen)).isTrue();
        assertThat(UserFilters.eligibleNames(List.of(eighteen))).containsExactly("Eve");
    }

    @Test
    void aMissingEmailIsNeverVerified() {
        User noEmail = new User("Finn", 30, true, null, true);

        assertThat(UserFilters.HAS_VERIFIED_EMAIL.test(noEmail)).isFalse();
        assertThat(UserFilters.IS_ELIGIBLE.test(noEmail)).isFalse();
    }

    @Test
    void eligibleNeedsEveryRule() {
        User minorVerified = new User("Gil", 15, true, "gil@example.com", true);
        User inactiveVerified = new User("Hana", 35, false, "hana@example.com", true);

        assertThat(UserFilters.IS_ELIGIBLE.test(minorVerified)).isFalse();
        assertThat(UserFilters.IS_ELIGIBLE.test(inactiveVerified)).isFalse();
    }
}
