package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TagIndexTest {

    // The same tags as two devices send them: an accented letter as one code point, or as the
    // base letter followed by a combining mark.
    private static final String CREME_ONE = "cr\u00E8me";          // one code point, U+00E8
    private static final String CREME_TWO = "cre\u0300me";         // e + combining grave U+0300
    private static final String JALAPENO_ONE = "jalape\u00F1o";    // one code point, U+00F1
    private static final String JALAPENO_TWO = "jalapen\u0303o";   // n + combining tilde U+0303

    @Test
    void countsPostsPerTagAndFindsThem() {
        TagIndex index = new TagIndex();
        index.add("sourdough");
        index.add("baking");
        index.add(new String("sourdough".toCharArray()));

        assertThat(index.counts()).containsExactly(
                java.util.Map.entry("sourdough", 2), java.util.Map.entry("baking", 1));
        assertThat(index.postsTagged(new String("baking".toCharArray()))).isEqualTo(1);
        assertThat(index.postsTagged("pastry")).isZero();
    }

    @Test
    void oneTagTypedOnTwoDevicesIsOneEntry() {
        TagIndex index = new TagIndex();
        index.add(CREME_ONE);
        index.add(CREME_TWO);

        assertThat(index.counts()).hasSize(1);
        assertThat(index.counts().values()).containsExactly(2);
    }

    @Test
    void tagsThatDifferOnlyInAnAccentStayApart() {
        TagIndex index = new TagIndex();
        index.add("rose");
        index.add("ros\u00E9");
        index.add("rose\u0301");

        assertThat(index.counts()).hasSize(2);
        assertThat(index.postsTagged("rose")).isEqualTo(1);
    }

    @Test
    void aSearchTypedOnAnotherDeviceFindsTheTag() {
        TagIndex index = new TagIndex();
        index.add(JALAPENO_ONE);
        index.add(JALAPENO_ONE);

        assertThat(index.postsTagged(JALAPENO_TWO)).isEqualTo(2);
    }
}
