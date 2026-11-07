package practice;

import org.junit.jupiter.api.Test;
import practice.Checkout.Adjustment;
import practice.Checkout.Parcel;
import practice.Checkout.Rule;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class CheckoutTest {

    private final List<String> asked = new ArrayList<>();

    /** A rule that answers {@code answer} and records that it was asked. */
    private Rule answering(String name, boolean answer) {
        return parcel -> {
            asked.add(name);
            return answer;
        };
    }

    private static final Parcel UTRECHT = new Parcel("NL", "NL-3511", 1_200);

    @Test
    void combinesRulesAndAdjustments() {
        Rule nextDay = Checkout.nextDay();
        assertThat(nextDay.accepts(new Parcel("NL", "NL-1012", 4_999))).isTrue();
        assertThat(nextDay.accepts(new Parcel("NL", "NL-1012", 5_000))).isTrue();
        assertThat(nextDay.accepts(new Parcel("NL", "NL-1012", 5_001))).isFalse();
        assertThat(nextDay.accepts(new Parcel("BE", "BE-2000", 800))).isFalse();

        assertThat(answering("a", true).and(answering("b", true)).accepts(UTRECHT)).isTrue();
        assertThat(answering("a", true).and(answering("b", false)).accepts(UTRECHT)).isFalse();
        assertThat(answering("a", false).and(answering("b", false)).accepts(UTRECHT)).isFalse();
        assertThat(answering("a", false).or(answering("b", true)).accepts(UTRECHT)).isTrue();
        assertThat(answering("a", false).or(answering("b", false)).accepts(UTRECHT)).isFalse();
        assertThat(answering("a", true).or(answering("b", false)).accepts(UTRECHT)).isTrue();

        Adjustment plus120 = cents -> cents + 120;
        Adjustment plus35 = cents -> cents + 35;
        assertThat(plus120.then(plus35).apply(1_000)).isEqualTo(1_155);
    }

    @Test
    void andLeavesTheOtherRuleUnaskedOnceThisOneRejects() {
        boolean accepted = answering("first", false).and(answering("second", true)).accepts(UTRECHT);

        assertThat(accepted).isFalse();
        assertThat(asked).containsExactly("first");
        assertThatCode(() -> Checkout.nextDay().accepts(new Parcel("NL", null, 300)))
                .doesNotThrowAnyException();
    }

    @Test
    void andAsksThisRuleBeforeTheOther() {
        answering("first", true).and(answering("second", true)).accepts(UTRECHT);

        assertThat(asked).containsExactly("first", "second");
    }

    @Test
    void orLeavesTheOtherRuleUnaskedOnceThisOneAccepts() {
        boolean accepted = answering("first", true).or(answering("second", false)).accepts(UTRECHT);

        assertThat(accepted).isTrue();
        assertThat(asked).containsExactly("first");
    }

    @Test
    void thenRunsThisStepFirst() {
        assertThat(Checkout.price(20, 399).apply(12_500)).isEqualTo(10_399);
        assertThat(Checkout.price(15, 250).apply(4_000)).isEqualTo(3_650);
    }
}
