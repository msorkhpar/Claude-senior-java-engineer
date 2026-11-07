package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostLimitTest {

    private static final String PARTY = "🎉";     // U+1F389, one code point, two chars
    private static final String MUSIC = "𝄞";     // U+1D11E, one code point, two chars

    @Test
    void countsAPlainPost() {
        assertThat(new PostLimit(20).counter("Release 4.2 is out")).isEqualTo("18/20");
        assertThat(new PostLimit(20).counter("")).isEqualTo("0/20");
    }

    @Test
    void cutsAPlainPostThatIsTooLong() {
        assertThat(new PostLimit(10).preview("Maintenance tonight at 22:00")).isEqualTo("Maintenanc");
        assertThat(new PostLimit(10).preview("Short")).isEqualTo("Short");
    }

    @Test
    void anEmojiCountsAsOneCharacter() {
        String post = "Launch day " + PARTY + PARTY + " and " + MUSIC;

        assertThat(new PostLimit(30).counter(post)).isEqualTo("19/30");
        assertThat(new PostLimit(19).preview(post)).isEqualTo(post);
    }

    @Test
    void aCutNeverSplitsAnEmoji() {
        String post = "Done " + PARTY + PARTY + PARTY + " thanks all";

        String preview = new PostLimit(7).preview(post);

        assertThat(preview).isEqualTo("Done " + PARTY + PARTY);
    }
}
