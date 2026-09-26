package practice;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ChecksTest {

    @Test
    void theComposedChainAppliesEveryRule() {
        Checks.Validator<String> validator = new Checks.MinLengthValidator(
                new Checks.NonEmptyValidator(new Checks.NonNullValidator()), 5);
        assertThat(validator.isValid("")).isFalse();
        assertThat(validator.isValid("abc")).isFalse();
        assertThat(validator.isValid("hello")).isTrue();
        assertThat(validator.isValid("hello world")).isTrue();
        assertThat(new Checks.NonEmptyValidator(new Checks.NonNullValidator()).isValid("x")).isTrue();
        assertThat(new Checks.NonEmptyValidator(new Checks.NonNullValidator()).isValid("  ")).isTrue();
        assertThat(new Checks.MinLengthValidator(new Checks.NonNullValidator(), 5).isValid("  abc")).isTrue();
        assertThat(new Checks.MinLengthValidator(new Checks.NonNullValidator(), 5).isValid("abcd")).isFalse();
    }

    @Test
    void aDecoratorAsksTheValidatorItWrapsFirst() {
        Checks.Validator<String> validator = new Checks.MinLengthValidator(
                new Checks.NonEmptyValidator(new Checks.NonNullValidator()), 5);
        assertThat(validator.isValid(null)).isFalse();
        assertThat(new Checks.MinLengthValidator(new Checks.NonNullValidator(), 3).isValid(null)).isFalse();
        assertThat(new Checks.NonEmptyValidator(new Checks.NonNullValidator()).isValid(null)).isFalse();
        List<String> asked = new ArrayList<>();
        Checks.Validator<String> recording = value -> { asked.add(value); return true; };
        assertThat(new Checks.MinLengthValidator(recording, 5).isValid("abc")).isFalse();
        assertThat(new Checks.NonEmptyValidator(recording).isValid("")).isFalse();
        assertThat(asked).containsExactly("abc", "");
    }

    @Test
    void anyValidatorCanBeDecorated() {
        Checks.Validator<String> startsWithA = value -> value != null && value.startsWith("A");
        Checks.Validator<String> validator = new Checks.MinLengthValidator(startsWithA, 3);
        assertThat(validator.isValid("Anna")).isTrue();
        assertThat(validator.isValid("Al")).isFalse();
        assertThat(validator.isValid("Bob12")).isFalse();
        assertThat(new Checks.NonEmptyValidator(startsWithA).isValid("Zed")).isFalse();
    }

    @Test
    void lengthCountsCharsNotCodePoints() throws Exception {
        Checks.Validator<String> fourChars = new Checks.MinLengthValidator(new Checks.NonNullValidator(), 4);
        assertThat(fourChars.isValid("\uD83D\uDE00\uD83D\uDE00")).as("two emoji are four chars").isTrue();
    }
}
