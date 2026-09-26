package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommandsTest {

    /** An extension the library has never seen. */
    static class Shout extends Commands.Extension {
        @Override
        public String name() {
            return "shout";
        }

        @Override
        public String run() {
            return "HELLO";
        }
    }

    @Test
    void runsEachBuiltInCommand() {
        assertThat(Commands.execute(new Commands.Quit())).isEqualTo("bye");
        assertThat(Commands.execute(new Commands.Echo("hi"))).isEqualTo("hi");
        assertThat(Commands.execute(new Commands.Help())).isEqualTo("[help] commands: quit, echo");
    }

    @Test
    void anExtensionWrittenElsewhereRunsThroughItsOwnMethods() {
        assertThat(Commands.execute(new Shout())).isEqualTo("[shout] HELLO");
    }
}
