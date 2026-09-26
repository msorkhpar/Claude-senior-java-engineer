package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TagsTest {

    @Test
    void wrapsTheContent() {
        assertThat(Tags.wrap(new StringBuilder("hi"), "b").toString()).isEqualTo("<b>hi</b>");
        assertThat(Tags.wrap(new StringBuilder(), "p").toString()).isEqualTo("<p></p>");
    }

    @Test
    void theCallersBuilderChanges() {
        StringBuilder sb = new StringBuilder("hi");
        assertThat(Tags.wrap(sb, "b")).isSameAs(sb);
        assertThat(sb.toString()).isEqualTo("<b>hi</b>");
    }

    @Test
    void wrappingTwiceNests() {
        StringBuilder sb = new StringBuilder("hi");
        Tags.wrap(Tags.wrap(sb, "b"), "i");
        assertThat(sb.toString()).isEqualTo("<i><b>hi</b></i>");
    }
}
