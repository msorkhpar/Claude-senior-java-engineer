package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class ValidatorsTest {

    @Test
    void acceptsGoodInputAndRejectsBadInput() {
        Predicate<String> email = Validators.email();
        assertThat(email.test("user@example.com")).isTrue();
        assertThat(email.test("not-an-email")).isFalse();
        assertThat(email.test("")).isFalse();
        assertThat(List.of("a@example.org", "invalid", "c@example.org", "bad").stream().filter(email).toList())
                .containsExactly("a@example.org", "c@example.org");

        Predicate<String> strong = Validators.strongPassword();
        assertThat(strong.test("Passw0rd!")).isTrue();
        assertThat(strong.test("password")).isFalse();
    }

    @Test
    void nullIsInvalidNotAnError() {
        assertThat(Validators.email().test(null)).isFalse();
        assertThat(Validators.strongPassword().test(null)).isFalse();
    }

    @Test
    void theAtSignNeedsSomethingBeforeIt() {
        assertThat(Validators.email().test("@example.com")).isFalse();
        assertThat(Validators.email().test("a@example.com")).isTrue();
    }

    @Test
    void theDomainNeedsADotAfterSomeText() {
        assertThat(Validators.email().test("user@example")).isFalse();
        assertThat(Validators.email().test("user@.com")).isFalse();
        assertThat(Validators.email().test("first.last@example")).isFalse();
        assertThat(Validators.email().test("user@x.io")).isTrue();
    }

    @Test
    void everyPasswordRuleCounts() {
        Predicate<String> strong = Validators.strongPassword();
        assertThat(strong.test("Pa0!")).isFalse();
        assertThat(strong.test("password1!")).isFalse();
        assertThat(strong.test("PASSWORD1!")).isFalse();
        assertThat(strong.test("Password!!")).isFalse();
        assertThat(strong.test("Password12")).isFalse();
        assertThat(strong.test("Pass word1")).isTrue();
    }

    @Test
    void lettersBeyondAsciiCount() {
        assertThat(Validators.strongPassword().test("\u00c4rger123!")).isTrue();
    }

    @Test
    void dotsBeforeTheAtAreAllowed() {
        assertThat(Validators.email().test("first.last@example.com")).isTrue();
    }

    @Test
    void eightCharactersAreEnough() {
        assertThat(Validators.strongPassword().test("Passw0r!")).isTrue();
    }

    @Test
    void theRuleIsFirstAtAndLastDot() {
        assertThat(Validators.email().test("a@b@c.com")).isTrue();
        assertThat(Validators.email().test("user@.example.com")).isTrue();
    }
}
