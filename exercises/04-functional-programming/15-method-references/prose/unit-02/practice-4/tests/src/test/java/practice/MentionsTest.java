package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MentionsTest {

    @Test
    void keepsExactMatchesAndAllowedValues() {
        assertThat(Mentions.mentioning("Java", List.of("java", "JAVA"))).isEmpty();
        assertThat(Mentions.allowed(List.of("a", "a", "x"), List.of("a"))).containsExactly("a", "a");
        assertThat(Mentions.mentioning("java", List.of("java", "go"))).containsExactly("java");
        assertThat(Mentions.prefixesOf("java", List.of("go", "java"))).containsExactly("java");
        assertThat(Mentions.allowed(List.of("a", "x", "b"), List.of("a", "b"))).containsExactly("a", "b");
    }

    @Test
    void aSourceLongerThanTheTermStillMentionsIt() {
        assertThat(Mentions.mentioning("searchTerm",
                List.of("find searchTerm here", "nothing", "also searchTerm")))
                .containsExactly("find searchTerm here", "also searchTerm");
    }

    @Test
    void aCandidateLongerThanTheTextIsNoPrefix() {
        assertThat(Mentions.prefixesOf("javascript", List.of("java", "script", "j", "javascript!", "")))
                .containsExactly("java", "j", "");
    }

    @Test
    void allowedValuesMatchByEquals() {
        List<String> requested = List.of(new String("admin"), new String("guest"));
        List<String> allowList = List.of(new String("guest"), new String("admin"));
        assertThat(Mentions.allowed(requested, allowList)).containsExactly("admin", "guest");
    }
}
