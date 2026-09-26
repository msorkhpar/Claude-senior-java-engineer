package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class TagsTest {

    /** A tag built at run time, so no two tags share one String object. */
    private static String tag(String text) {
        return new String(text.toCharArray());
    }

    @Test
    void flattensUppercasesAndSorts() {
        List<List<String>> posts = List.of(List.of(tag("streams"), tag("java")), List.of(tag("lambda")));
        assertThat(Tags.allTags(posts)).containsExactly("JAVA", "LAMBDA", "STREAMS");
        assertThat(Tags.allTags(List.of())).isEmpty();
    }

    @Test
    void tagsThatDifferOnlyInCaseMerge() {
        List<List<String>> posts = List.of(List.of(tag("Java")), List.of(tag("JAVA"), tag("jvm")));
        assertThat(Tags.allTags(posts)).containsExactly("JAVA", "JVM");
    }

    @Test
    void repeatedTagsAppearOnce() {
        List<List<String>> posts = List.of(
                List.of(tag("JVM"), tag("GC")),
                List.of(tag("JVM")),
                List.of(tag("GC"), tag("JIT")));
        assertThat(Tags.allTags(posts)).containsExactly("GC", "JIT", "JVM");
    }

    @Test
    void emptyTagsAreSkipped() {
        List<List<String>> posts = List.of(List.of(tag(""), tag("x")), List.of(tag("")));
        assertThat(Tags.allTags(posts)).containsExactly("X");
        List<List<String>> spaced = List.of(List.of(tag(" "), tag("x"), tag("")));
        assertThat(Tags.allTags(spaced)).containsExactly(" ", "X");
    }

    @Test
    void upperCasingIgnoresTheDefaultLocale() {
        Locale saved = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            List<List<String>> posts = List.of(List.of(tag("title"), tag("vim")));
            assertThat(Tags.allTags(posts)).containsExactly("TITLE", "VIM");
        } finally {
            Locale.setDefault(saved);
        }
    }

    @Test
    void nullTagsAreSkipped() {
        List<String> withNull = new ArrayList<>(Arrays.asList(tag("b"), null, tag("a")));
        List<List<String>> posts = List.of(withNull, new ArrayList<>(Arrays.asList((String) null)));
        assertThat(Tags.allTags(posts)).containsExactly("A", "B");
    }
}
