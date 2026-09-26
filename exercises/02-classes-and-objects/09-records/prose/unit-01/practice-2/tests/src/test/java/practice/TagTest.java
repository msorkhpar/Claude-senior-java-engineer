package practice;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TagTest {

    @Test
    void aTagIsStoredTrimmedAndLowerCase() {
        assertThat(new Tag("  Java ").value()).isEqualTo("java");
        assertThat(new Tag("records").value()).isEqualTo("records");
    }

    @Test
    void tagsThatDifferOnlyInCaseAreEqual() {
        Tag upper = new Tag("RECORDS");
        Tag lower = new Tag(" records");
        assertThat(upper).isEqualTo(lower);
        assertThat(upper.hashCode()).isEqualTo(lower.hashCode());
        Set<Tag> tags = new HashSet<>();
        tags.add(new Tag("Java"));
        tags.add(new Tag(" java"));
        assertThat(tags).hasSize(1);
    }

    @Test
    void aBlankTagIsRefused() {
        assertThatThrownBy(() -> new Tag("   ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aNullTagIsRefused() {
        assertThatThrownBy(() -> new Tag(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
