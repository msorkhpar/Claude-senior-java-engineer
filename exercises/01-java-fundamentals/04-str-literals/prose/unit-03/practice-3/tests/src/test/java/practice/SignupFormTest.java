package practice;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SignupFormTest {

    private static Map<String, String> form(String... pairs) {
        Map<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) m.put(pairs[i], pairs[i + 1]);
        return m;
    }

    @Test
    void removesTypedSpacesTabsAndLineBreaks() {
        Map<String, String> saved = SignupForm.accept(
                form("first", "  Priya ", "last", "\tNair\n", "city", " Kochi"), List.of("first", "last"));

        assertThat(saved).containsExactly(Map.entry("first", "Priya"), Map.entry("last", "Nair"), Map.entry("city", "Kochi"));
    }

    @Test
    void anEmptyOrSpacesOnlyRequiredFieldIsMissing() {
        assertThatThrownBy(() -> SignupForm.accept(form("first", "Priya", "last", "   "), List.of("first", "last")))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("missing: last");
        assertThatThrownBy(() -> SignupForm.accept(form("first", "", "last", "Nair"), List.of("first", "last")))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("missing: first");
    }

    @Test
    void anOptionalFieldMayBeBlank() {
        Map<String, String> saved = SignupForm.accept(form("first", "Priya", "city", "  "), List.of("first"));

        assertThat(saved).containsEntry("city", "");
    }

    @Test
    void removesNoBreakSpacesPastedFromAWebPage() {
        Map<String, String> saved = SignupForm.accept(
                form("first", " Tomás ", "last", "Ortega "), List.of("first", "last"));

        assertThat(saved).containsExactly(Map.entry("first", "Tomás"), Map.entry("last", "Ortega"));
    }

    @Test
    void removesWideSpacesPastedFromADocument() {
        Map<String, String> saved = SignupForm.accept(
                form("first", "　Yuki", "last", "Sato "), List.of("first", "last"));

        assertThat(saved).containsExactly(Map.entry("first", "Yuki"), Map.entry("last", "Sato"));
    }

    @Test
    void aFieldOfNoBreakSpacesIsMissing() {
        assertThatThrownBy(() -> SignupForm.accept(form("first", "Ines", "last", "  "), List.of("first", "last")))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("missing: last");
    }

    @Test
    void keepsTheSpacesBetweenWords() {
        Map<String, String> saved = SignupForm.accept(
                form("first", " Mary Ann ", "last", "van der Berg"), List.of("first", "last"));

        assertThat(saved).containsExactly(Map.entry("first", "Mary Ann"), Map.entry("last", "van der Berg"));
    }
}
