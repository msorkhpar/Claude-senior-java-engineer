package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EligibilityTest {

    private static final List<User> USERS = List.of(
            new User("alice", "alice@example.com", true, 25),
            new User("bob", "", false, 30),
            new User("charlie", "c@example.com", false, 16),
            new User("diana", "d@example.com", true, 22));

    @Test
    void findsEligibleAndUnverifiedUsers() {
        assertThat(Eligibility.eligible(USERS)).containsExactly("alice", "diana");
        String[] unverified = Eligibility.unverified(USERS);
        assertThat(unverified).containsExactly("bob", "charlie");
        assertThat(unverified.getClass().getComponentType()).isEqualTo(String.class);
        assertThat(Eligibility.isAdult(new User("x", "x@example.com", true, 40))).isTrue();
        assertThat(Eligibility.hasEmail(new User("x", "x@example.com", true, 40))).isTrue();
    }

    @Test
    void eighteenIsAdult() {
        User frank = new User("frank", "f@example.com", true, 18);
        assertThat(Eligibility.isAdult(frank)).isTrue();
        assertThat(Eligibility.eligible(List.of(frank))).containsExactly("frank");
    }

    @Test
    void aBlankEmailIsNoEmail() {
        User eve = new User("eve", "   ", true, 30);
        assertThat(Eligibility.hasEmail(eve)).isFalse();
        assertThat(Eligibility.eligible(List.of(eve))).isEmpty();
    }

    @Test
    void aNullEmailIsNoEmail() {
        User gil = new User("gil", null, true, 30);
        assertThat(Eligibility.hasEmail(gil)).isFalse();
        assertThat(Eligibility.eligible(List.of(gil))).isEmpty();
    }
}
