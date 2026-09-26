package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class RepliesTest {

    @Test
    void knownMessagesGetTheirReply() {
        assertThat(Replies.reply("Hello\nWorld")).isEqualTo("greeting");
        assertThat(Replies.reply("Bye")).isEqualTo("farewell");
        assertThat(Replies.reply("Hello")).isEqualTo("unknown");
        assertThat(Replies.reply("")).isEqualTo("unknown");
        assertThat(Replies.reply("hello\nworld")).isEqualTo("unknown");
        assertThat(Replies.reply("BYE")).isEqualTo("unknown");
    }

    @Test
    void aMessageBuiltAtRunTimeMatchesByContent() {
        String built = new StringBuilder("Hello").append('\n').append("World").toString();
        assertThat(Replies.reply(built)).isEqualTo("greeting");
        assertThat(Replies.reply("%s".formatted("Bye"))).isEqualTo("farewell");
        assertThat(Replies.reply(String.join("", "B", "ye"))).isEqualTo("farewell");
    }

    @Test
    void aTrailingNewlineMakesAnotherMessage() {
        assertThat(Replies.reply(String.join("\n", "Hello", "World", ""))).isEqualTo("unknown");
        assertThat(Replies.reply(String.join("", "Bye", " "))).isEqualTo("unknown");
    }

    @Test
    void noMessageIsUnknown() {
        assertThatCode(() -> Replies.reply(null)).doesNotThrowAnyException();
        assertThat(Replies.reply(null)).isEqualTo("unknown");
    }
}
