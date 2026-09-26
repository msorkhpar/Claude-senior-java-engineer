package practice;

import org.junit.jupiter.api.Test;

import practice.InlinePolicy.Decision;

import static org.assertj.core.api.Assertions.assertThat;

class InlinePolicyTest {

    private final InlinePolicy policy = InlinePolicy.defaults();

    @Test
    void decidesThePagesCallSites() {
        assertThat(policy.decide(4, false, 1)).isEqualTo(Decision.INLINE);
        assertThat(policy.decide(36, true, 1)).isEqualTo(Decision.INLINE);
        assertThat(policy.decide(2000, true, 1)).isEqualTo(Decision.TOO_BIG);
        assertThat(policy.decide(20, true, 3)).isEqualTo(Decision.MEGAMORPHIC);
        assertThat(policy.decide(2000, true, 3)).isEqualTo(Decision.MEGAMORPHIC);
    }

    @Test
    void aColdSiteKeepsTheSmallLimit() {
        assertThat(policy.decide(36, false, 1)).isEqualTo(Decision.TOO_BIG);
        assertThat(policy.decide(200, false, 1)).isEqualTo(Decision.TOO_BIG);
    }

    @Test
    void theLimitsAreInclusive() {
        assertThat(policy.decide(35, false, 1)).isEqualTo(Decision.INLINE);
        assertThat(policy.decide(325, true, 1)).isEqualTo(Decision.INLINE);
        assertThat(policy.decide(326, true, 1)).isEqualTo(Decision.TOO_BIG);
    }

    @Test
    void twoReceiverTypesStillInline() {
        assertThat(policy.decide(20, true, 2)).isEqualTo(Decision.INLINE);
        assertThat(policy.decide(20, false, 2)).isEqualTo(Decision.INLINE);
        assertThat(policy.decide(20, false, 4)).isEqualTo(Decision.MEGAMORPHIC);
    }

    @Test
    void theLimitsComeFromThePolicy() {
        InlinePolicy tight = new InlinePolicy(10, 100);

        assertThat(tight.decide(20, false, 1)).isEqualTo(Decision.TOO_BIG);
        assertThat(tight.decide(20, true, 1)).isEqualTo(Decision.INLINE);
        assertThat(tight.decide(101, true, 1)).isEqualTo(Decision.TOO_BIG);
    }
}
