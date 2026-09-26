package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CommandsTest {

    @Test
    void recognisesTheCommand() {
        assertThat(Commands.isQuit("quit")).isTrue();
        assertThat(Commands.isQuit("quite")).isFalse();
        assertThat(Commands.isQuit("")).isFalse();
    }

    @Test
    void caseDoesNotMatter() {
        assertThat(Commands.isQuit("QUIT")).isTrue();
        assertThat(Commands.isQuit("Quit")).isTrue();
    }

    @Test
    void surroundingSpacesDoNotMatter() {
        assertThat(Commands.isQuit("  quit ")).isTrue();
    }

    @Test
    void nullIsNotTheCommand() {
        assertThat(Commands.isQuit(null)).isFalse();
    }
}
