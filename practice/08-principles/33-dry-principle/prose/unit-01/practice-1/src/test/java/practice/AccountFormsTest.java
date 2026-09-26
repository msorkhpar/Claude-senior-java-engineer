package practice;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.*;

class AccountFormsTest {

    @Test
    void everyFormAppliesTheSameRule() {
        AccountForms forms = new AccountForms();
        for (String good : List.of("user@example.com", "a.b@mail.example", new String("first.last@localhost"))) {
            assertThat(AccountForms.isValidEmail(good)).as(good).isTrue();
            assertThat(forms.register(good)).as("register " + good).isTrue();
            assertThat(forms.resetPassword(good)).as("resetPassword " + good).isTrue();
            assertThat(forms.changeEmail(good)).as("changeEmail " + good).isTrue();
        }
        for (String bad : List.of("userexample.com", "user@examplecom")) {
            assertThat(AccountForms.isValidEmail(bad)).as(bad).isFalse();
            assertThat(forms.register(bad)).as("register " + bad).isFalse();
            assertThat(forms.resetPassword(bad)).as("resetPassword " + bad).isFalse();
            assertThat(forms.changeEmail(bad)).as("changeEmail " + bad).isFalse();
        }
    }

    @Test
    void nullAndBlankAreRejectedWithoutThrowing() {
        AccountForms forms = new AccountForms();
        for (String bad : Arrays.asList(null, "", "   ")) {
            assertThat(AccountForms.isValidEmail(bad)).as(String.valueOf(bad)).isFalse();
            assertThat(forms.register(bad)).isFalse();
            assertThat(forms.resetPassword(bad)).isFalse();
            assertThat(forms.changeEmail(bad)).isFalse();
        }
    }

    @Test
    void aReplacedRuleReachesEveryForm() {
        Predicate<String> corporate = e -> e != null && e.endsWith("@corp.example");
        AccountForms forms = new AccountForms(corporate);
        assertThat(forms.register("ann@corp.example")).isTrue();
        assertThat(forms.resetPassword("ann@corp.example")).isTrue();
        assertThat(forms.changeEmail("ann@corp.example")).isTrue();
        assertThat(forms.register("ann@example.com")).as("register").isFalse();
        assertThat(forms.resetPassword("ann@example.com")).as("resetPassword").isFalse();
        assertThat(forms.changeEmail("ann@example.com")).as("changeEmail").isFalse();
        AccountForms startsWithX = new AccountForms(e -> e != null && e.startsWith("x"));
        assertThat(startsWithX.register("xyz")).as("register xyz").isTrue();
        assertThat(startsWithX.resetPassword("xyz")).as("resetPassword xyz").isTrue();
        assertThat(startsWithX.changeEmail("xyz")).as("changeEmail xyz").isTrue();
        AccountForms acceptAll = new AccountForms(e -> true);
        for (String any : Arrays.asList(null, "  ")) {
            assertThat(acceptAll.register(any)).as("register " + any).isTrue();
            assertThat(acceptAll.resetPassword(any)).as("resetPassword " + any).isTrue();
            assertThat(acceptAll.changeEmail(any)).as("changeEmail " + any).isTrue();
        }
    }
}
