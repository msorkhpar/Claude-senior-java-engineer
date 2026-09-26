package practice;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TagTest {

    /** Builds the string at run time so no two inputs share one interned literal. */
    private static String fresh(String s) {
        return new StringBuilder(s).toString();
    }

    @Test
    void tagsThatDifferOnlyInCaseAreEqual() {
        Tag java = new Tag(fresh("Java"));

        assertThat(java).isEqualTo(new Tag(fresh("JAVA")));
        assertThat(java).isEqualTo(new Tag(fresh("java")));
        assertThat(java).isNotEqualTo(new Tag(fresh("Kotlin")));
        assertThat(java.name()).isEqualTo("Java");
        assertThatThrownBy(() -> new Tag(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void aHashSetKeepsOneTagPerName() {
        Set<Tag> tags = new HashSet<>();
        for (String s : List.of("Java", "JAVA", "java")) {
            tags.add(new Tag(fresh(s)));
        }

        assertThat(tags).hasSize(1);
        assertThat(tags.contains(new Tag(fresh("jAvA")))).isTrue();

        String[][] tricky = {{"\u212A", "k"}, {"\u0130", "i"}, {"Stra\u00dfe", "STRASSE"}, {"\u01C5", "\u01C6"}};
        for (String[] pair : tricky) {
            Tag a = new Tag(fresh(pair[0]));
            Tag b = new Tag(fresh(pair[1]));
            if (a.equals(b)) {
                assertThat(a.hashCode()).as("hash codes of equal tags %s and %s", pair[0], pair[1]).isEqualTo(b.hashCode());
            }
        }
    }

    @Test
    void comparingWithAnythingElseIsFalse() {
        Tag java = new Tag(fresh("Java"));

        assertThat(java.equals(null)).isFalse();
        assertThat(java.equals((Object) fresh("Java"))).isFalse();
    }

    @Test
    void theDefaultLocaleDoesNotMatter() {
        Locale before = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            Tag upper = new Tag(fresh("TITLE"));
            Tag lower = new Tag(fresh("title"));

            assertThat(upper).isEqualTo(lower);
            assertThat(upper.hashCode()).isEqualTo(lower.hashCode());
            assertThat(new HashSet<>(List.of(upper, lower))).hasSize(1);
        } finally {
            Locale.setDefault(before);
        }
    }
}
