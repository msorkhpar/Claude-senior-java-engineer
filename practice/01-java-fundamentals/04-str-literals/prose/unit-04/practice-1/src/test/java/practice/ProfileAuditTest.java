package practice;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileAuditTest {

    private final ProfileAudit audit = new ProfileAudit();

    private static Map<String, String> stored(String... pairs) {
        Map<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) m.put(pairs[i], pairs[i + 1]);
        return m;
    }

    private static Map<String, CharSequence> submitted(Object... pairs) {
        Map<String, CharSequence> m = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) m.put((String) pairs[i], (CharSequence) pairs[i + 1]);
        return m;
    }

    /** The same text as a new String object, so no test passes by comparing an object with itself. */
    private static String fresh(String s) {
        return new String(s.toCharArray());
    }

    @Test
    void reportsTheFieldsWhoseTextChanged() {
        Map<String, String> before = stored("displayName", "Kai Moreno", "city", "Porto", "website", "https://example.com/kai");
        Map<String, CharSequence> after = submitted(
                "displayName", fresh("Kai Moreno"),
                "city", fresh("Braga"),
                "website", fresh("https://example.com/kai"),
                "bio", fresh("Climber."));

        assertThat(audit.changedFields(before, after)).containsExactly("city", "bio");
    }

    @Test
    void textHeldInAnEditorBufferIsComparedByItsCharacters() {
        Map<String, String> before = stored("displayName", "Kai Moreno", "city", "Porto");
        Map<String, CharSequence> after = submitted(
                "displayName", new StringBuilder("Kai ").append("Moreno"),
                "city", new StringBuilder("Bra").append("ga"));

        assertThat(audit.changedFields(before, after)).containsExactly("city");
    }

    @Test
    void aFieldThatWasEmptyAndStaysEmptyIsNotAChange() {
        Map<String, String> before = stored("city", "Porto", "pronouns", null);
        Map<String, CharSequence> after = submitted("city", fresh("Porto"), "pronouns", null);

        assertThat(audit.changedFields(before, after)).isEmpty();
    }

    @Test
    void clearingAFieldIsAChange() {
        Map<String, String> before = stored("city", "Porto", "website", "https://example.com/kai");
        Map<String, CharSequence> after = submitted("city", fresh("Porto"), "website", null);

        assertThat(audit.changedFields(before, after)).containsExactly("website");
    }
}
