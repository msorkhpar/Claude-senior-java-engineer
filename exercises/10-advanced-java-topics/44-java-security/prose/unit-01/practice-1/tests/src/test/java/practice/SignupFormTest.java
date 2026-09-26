package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SignupFormTest {

    private static void refusedName(String raw) {
        assertThatThrownBy(() -> SignupForm.username(raw))
                .as("username %s", raw)
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static void refusedAge(String raw) {
        assertThatThrownBy(() -> SignupForm.age(raw))
                .as("age %s", raw)
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void acceptsValidFieldsAndRefusesBadOnes() {
        assertThat(SignupForm.username("alice99")).isEqualTo("alice99");
        assertThat(SignupForm.age("42")).isEqualTo(42);
        refusedName(null);
        refusedName("a b");
        refusedAge(null);
        refusedAge("abc");
    }

    @Test
    void theWholeInputMustMatch() {
        refusedName("bob!");
        refusedName("bob\nrm");
        refusedName("<script>bob");
    }

    @Test
    void lengthIsThreeToTwentyInclusive() {
        assertThat(SignupForm.username("abc")).isEqualTo("abc");
        assertThat(SignupForm.username("a".repeat(20))).isEqualTo("a".repeat(20));
        refusedName("ab");
        refusedName("a".repeat(21));
    }

    @Test
    void inputIsNormalizedBeforeValidation() {
        String fullWidth = "ａｌｉｃｅ"; // ａｌｉｃｅ
        assertThat(SignupForm.username(fullWidth)).isEqualTo("alice");
        assertThat(SignupForm.username("user１２")).isEqualTo("user12");
    }

    @Test
    void onlyAsciiLettersAndDigitsPass() {
        refusedName("аdmin");      // Cyrillic small a
        refusedName("café");       // é stays é after NFKC
        refusedName("user٣٤"); // Arabic-Indic digits
        refusedName("user_name");
    }

    @Test
    void anAgeIsOneToThreeAsciiDigits() {
        assertThat(SignupForm.age("0")).isZero();
        assertThat(SignupForm.age("150")).isEqualTo(150);
        refusedAge("151");
        refusedAge("-1");
        refusedAge("+42");
        refusedAge("٤٢"); // Arabic-Indic 42, which Integer.parseInt accepts
        refusedAge("");
    }
}
