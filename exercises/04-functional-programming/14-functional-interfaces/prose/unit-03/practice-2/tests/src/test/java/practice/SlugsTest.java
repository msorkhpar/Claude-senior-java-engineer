package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class SlugsTest {

    @Test
    void slugifiesOrdinaryTitles() {
        Function<String, String> slugify = Slugs.slugifier();
        assertThat(slugify.apply("  Hello, World!  ")).isEqualTo("hello-world");
        assertThat(slugify.apply("Java 21 Features!!!")).isEqualTo("java-21-features");
        assertThat(slugify.apply("state-of-the-art")).isEqualTo("state-of-the-art");
        assertThat(List.of("Hello World", "Java Is Great").stream().map(slugify).toList())
                .containsExactly("hello-world", "java-is-great");
    }

    @Test
    void runsOfSpacesBecomeOneHyphen() {
        assertThat(Slugs.slugifier().apply("  My   Blog   Post  ")).isEqualTo("my-blog-post");
        assertThat(Slugs.slugifier().apply("tabs\t\tand  spaces")).isEqualTo("tabs-and-spaces");
    }

    @Test
    void noStrayHyphenAtTheEnds() {
        assertThat(Slugs.slugifier().apply("Hello, World !")).isEqualTo("hello-world");
        assertThat(Slugs.slugifier().apply("# Title")).isEqualTo("title");
    }

    @Test
    void nullBecomesAnEmptySlug() {
        assertThat(Slugs.slugifier().apply(null)).isEmpty();
    }
}
