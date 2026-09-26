package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationRuleTest {

    @Test
    void validatesAgainstEachRule() {
        assertThat(ValidationRule.VALID_EMAIL.validate("team@example.org")).isEmpty();
        assertThat(ValidationRule.POSITIVE_NUMBER.validate(-1)).contains("Must be positive");
        assertThat(ValidationRule.POSITIVE_NUMBER.validate(2.5)).isEmpty();
        assertThat(ValidationRule.NOT_NULL.validate(null)).contains("Must not be null");
        assertThat(ValidationRule.validateAll("bad-email", ValidationRule.NOT_NULL, ValidationRule.NOT_BLANK, ValidationRule.VALID_EMAIL))
                .containsExactly("Must be a valid email");
        assertThat(ValidationRule.validateAll("ok", ValidationRule.NOT_NULL, ValidationRule.NOT_BLANK)).isEmpty();
        assertThat(ValidationRule.POSITIVE_NUMBER.validate(0.5)).isEmpty();
        assertThat(ValidationRule.VALID_EMAIL.validate("x y team@example.org")).contains("Must be a valid email");
    }

    @Test
    void everyViolationIsReported() {
        assertThat(ValidationRule.validateAll(-3, ValidationRule.NOT_BLANK, ValidationRule.POSITIVE_NUMBER, ValidationRule.VALID_EMAIL))
                .containsExactly("Must not be blank", "Must be positive", "Must be a valid email");
    }

    @Test
    void nullViolatesTheTextRules() {
        assertThat(ValidationRule.NOT_BLANK.validate(null)).contains("Must not be blank");
        assertThat(ValidationRule.VALID_EMAIL.validate(null)).contains("Must be a valid email");
        assertThat(ValidationRule.POSITIVE_NUMBER.validate(null)).contains("Must be positive");
    }

    @Test
    void blankIsNotTheSameAsEmpty() {
        assertThat(ValidationRule.NOT_BLANK.validate("   ")).contains("Must not be blank");
        assertThat(ValidationRule.NOT_BLANK.validate("\t")).contains("Must not be blank");
        assertThat(ValidationRule.NOT_BLANK.validate("\u2003")).contains("Must not be blank");
    }
}
