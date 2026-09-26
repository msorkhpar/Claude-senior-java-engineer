package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LevelParserTest {

    @Test
    void parsesTheKnownLevels() {
        assertThat(LevelParser.parseLevel(new String("low"))).isEqualTo(1);
        assertThat(LevelParser.parseLevel(new String("high"))).isEqualTo(3);
        assertThatThrownBy(() -> LevelParser.parseLevel(new String("medium")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theMessageNamesTheUnknownValue() {
        assertThatThrownBy(() -> LevelParser.parseLevel(new String("mid")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown level: mid");
    }

    @Test
    void nullIsAnUnknownLevelToo() {
        assertThatThrownBy(() -> LevelParser.parseLevel(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown level: null");
    }

    @Test
    void onlyTheExactSpellingIsKnown() {
        assertThatThrownBy(() -> LevelParser.parseLevel(new String("LOW")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown level: LOW");
        assertThatThrownBy(() -> LevelParser.parseLevel(new String(" low")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown level:  low");
    }
}
