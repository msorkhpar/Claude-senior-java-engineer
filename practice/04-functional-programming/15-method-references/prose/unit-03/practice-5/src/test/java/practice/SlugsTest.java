package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlugsTest {

    @Test
    void makesASlugAndMeasuresIt() {
        assertThat(Slugs.slug().apply("Hello, World!")).isEqualTo("hello,-world!");
        assertThat(Slugs.slug().apply("Hello World")).isEqualTo("hello-world");
        assertThat(Slugs.slug().apply("Java")).isEqualTo("java");
        assertThat(Slugs.slugLength().apply("Hello World")).isEqualTo(11);
        assertThat(Slugs.joinedLength().apply("Hello", "World")).isEqualTo(10);
        assertThat(Slugs.joinedLength().apply("", "")).isZero();
    }

    @Test
    void outerSpacesAreTrimmedBeforeDashing() {
        assertThat(Slugs.slug().apply("  Hello World ")).isEqualTo("hello-world");
    }

    @Test
    void aRunOfSpacesBecomesOneDash() {
        assertThat(Slugs.slug().apply("Hello\tWorld")).isEqualTo("hello-world");
        assertThat(Slugs.slug().apply("A \t\n B")).isEqualTo("a-b");
        assertThat(Slugs.slug().apply("Method   References")).isEqualTo("method-references");
    }

    @Test
    void theLengthIsTakenAfterTheSlug() {
        assertThat(Slugs.slugLength().apply("  Hi  ")).isEqualTo(2);
        assertThat(Slugs.slugLength().apply("A   B")).isEqualTo(3);
    }
}
