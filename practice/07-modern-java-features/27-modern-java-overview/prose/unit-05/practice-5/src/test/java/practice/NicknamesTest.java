package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class NicknamesTest {

    private static final Duration LIMIT = Duration.ofSeconds(10);
    private static final String ROCKET = new String(Character.toChars(0x1F680));   // U+1F680
    private static final String HEART = "❤";                                 // U+2764, one char

    @Test
    void plainNamesPassAndAHeartIsCaught() {
        assertTimeoutPreemptively(LIMIT, () -> {
            assertThat(Nicknames.hasPicture("marta")).isFalse();
            assertThat(Nicknames.hasPicture("Zoë_the-cat")).isFalse();
            assertThat(Nicknames.hasPicture("i" + HEART + "tea")).isTrue();
            assertThat(Nicknames.underline("marta")).isEqualTo("-----");
            assertThat(Nicknames.underline("i" + HEART + "tea")).isEqualTo("-----");
        });
    }

    @Test
    void aPictureOutsideTheBasicPlaneIsCaught() {
        assertTimeoutPreemptively(LIMIT, () -> {
            assertThat(Nicknames.hasPicture("liftoff" + ROCKET)).isTrue();
            assertThat(Nicknames.hasPicture(ROCKET)).isTrue();
        });
    }

    @Test
    void digitsAndKeycapSymbolsAreNotPictures() {
        assertTimeoutPreemptively(LIMIT, () -> {
            assertThat(Nicknames.hasPicture("agent_007")).isFalse();
            assertThat(Nicknames.hasPicture("#team*9")).isFalse();
        });
    }

    @Test
    void theUnderlineIsAsWideAsANameWithAPictureInIt() {
        assertTimeoutPreemptively(LIMIT, () -> {
            assertThat(Nicknames.underline("go" + ROCKET)).isEqualTo("---");
            assertThat(Nicknames.underline(ROCKET + ROCKET)).isEqualTo("--");
        });
    }
}
