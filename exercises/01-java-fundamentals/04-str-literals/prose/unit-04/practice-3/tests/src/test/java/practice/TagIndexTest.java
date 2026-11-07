package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TagIndexTest {

    private Locale before;

    @BeforeEach
    void rememberTheDefaultLocale() {
        before = Locale.getDefault();
    }

    @AfterEach
    void restoreTheDefaultLocale() {
        Locale.setDefault(before);
    }

    /** The same text as a new String object, so no test passes by comparing an object with itself. */
    private static String fresh(String s) {
        return new String(s.toCharArray());
    }

    @Test
    void countsPostsPerTagAndShowsEachAsFirstTyped() {
        TagIndex index = new TagIndex();
        index.add("sourdough");
        index.add("baking");
        index.add(fresh("sourdough"));

        assertThat(index.counts()).containsExactly(Map.entry("sourdough", 2), Map.entry("baking", 1));
        assertThat(index.postsTagged(fresh("baking"))).isEqualTo(1);
        assertThat(index.postsTagged("pastry")).isZero();
        assertThat(new TagIndex.Tag("sourdough")).isEqualTo(new TagIndex.Tag(fresh("sourdough")))
                .isNotEqualTo(new TagIndex.Tag("baking"));
    }

    @Test
    void aTagTypedInAnotherCaseIsTheSameTag() {
        TagIndex index = new TagIndex();
        index.add("Linux");
        index.add("LINUX");
        index.add("linux");

        assertThat(index.counts()).containsExactly(Map.entry("Linux", 3));
        assertThat(index.postsTagged("lInUx")).isEqualTo(3);
    }

    @Test
    void caseIsIgnoredOnAServerWithATurkishDefaultLocale() {
        Locale.setDefault(Locale.forLanguageTag("tr-TR"));
        TagIndex index = new TagIndex();
        index.add("WIKI");
        index.add("wiki");

        assertThat(index.counts()).hasSize(1);
        assertThat(index.postsTagged("Wiki")).isEqualTo(2);
    }

    @Test
    void tagsThatAreEqualHaveEqualHashCodes() {
        List<String[]> pairs = List.of(
                new String[] {"\u0131zmir", "IZMIR"},          // dotless i
                new String[] {"\u0130stanbul", "istanbul"},    // dotted capital I
                new String[] {"\u017Fushi", "SUSHI"},          // long s
                new String[] {"Stra\u00DFe", "STRASSE"});
        for (String[] p : pairs) {
            TagIndex.Tag a = new TagIndex.Tag(p[0]);
            TagIndex.Tag b = new TagIndex.Tag(p[1]);
            if (a.equals(b)) {
                assertThat(a.hashCode()).as("hash codes of %s and %s", p[0], p[1]).isEqualTo(b.hashCode());
            }
        }
    }

    @Test
    void anAccentedTagTypedOnAnotherDeviceIsTheSameTag() {
        TagIndex index = new TagIndex();
        index.add("CR\u00C8ME");           // one code point, U+00C8
        index.add("cre\u0300me");          // e + combining grave U+0300

        assertThat(index.counts()).hasSize(1);
        assertThat(index.postsTagged("Cr\u00E8me")).isEqualTo(2);
    }
}
