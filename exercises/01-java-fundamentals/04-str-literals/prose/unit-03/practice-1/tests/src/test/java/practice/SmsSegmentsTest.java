package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SmsSegmentsTest {

    private static final String GRINNING = "😀";
    private static final String ROCKET = "🚀";

    private static boolean wellFormed(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isHighSurrogate(c)) {
                if (i + 1 == s.length() || !Character.isLowSurrogate(s.charAt(i + 1))) return false;
                i++;
            } else if (Character.isLowSurrogate(c)) {
                return false;
            }
        }
        return true;
    }

    @Test
    void splitsPlainTextIntoFullSegments() {
        List<String> parts = SmsSegments.segments("Your parcel 4417 arrives Friday", 8);

        assertThat(parts).containsExactly("Your par", "cel 4417", " arrives", " Friday");
    }

    @Test
    void accentedLettersCountOnceEach() {
        List<String> parts = SmsSegments.segments("Grüße aus Köln, Zoë", 6);

        assertThat(parts).containsExactly("Grüße ", "aus Kö", "ln, Zo", "ë");
    }

    @Test
    void aShortTextIsOneSegment() {
        assertThat(SmsSegments.segments("OK", 70)).containsExactly("OK");
    }

    @Test
    void noSegmentEndsHalfwayThroughAnEmoji() {
        String text = "Done" + GRINNING + "see you" + ROCKET + "!";
        List<String> parts = SmsSegments.segments(text, 5);

        assertThat(String.join("", parts)).isEqualTo(text);
        assertThat(parts).allSatisfy(p -> assertThat(wellFormed(p)).as("well-formed: %s", p).isTrue());

        String exact = "Go" + ROCKET + "now";
        List<String> fitted = SmsSegments.segments(exact, 4);
        assertThat(String.join("", fitted)).isEqualTo(exact);
        assertThat(fitted).allSatisfy(p -> assertThat(wellFormed(p)).as("well-formed: %s", p).isTrue());
    }

    @Test
    void aSegmentOfEmojiStillFitsTheSegmentSize() {
        String text = ROCKET.repeat(5);
        List<String> parts = SmsSegments.segments(text, 4);

        assertThat(String.join("", parts)).isEqualTo(text);
        assertThat(parts).allSatisfy(p -> assertThat(p.length()).as("units in %s", p).isLessThanOrEqualTo(4));
    }
}
