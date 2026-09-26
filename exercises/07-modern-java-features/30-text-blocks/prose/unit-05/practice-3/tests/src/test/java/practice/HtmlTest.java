package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlTest {

    private static String card(String titleLine, String bodyLine) {
        return String.join("\n", "<div class=\"card\">", "    <h1>" + titleLine + "</h1>", "    <p>" + bodyLine + "</p>", "</div>");
    }

    @Test
    void fillsTheCard() {
        assertThat(Html.card(new String("Welcome"), new String("Hello!"))).isEqualTo(card("Welcome", "Hello!"));
        assertThat(Html.card(new String("It's here"), new String("Plain text only."))).isEqualTo(card("It's here", "Plain text only."));
    }

    @Test
    void theTitleIsEscapedToo() {
        assertThat(Html.card(new String("<script>alert('x')</script>"), new String("ok")))
                .isEqualTo(card("&lt;script&gt;alert('x')&lt;/script&gt;", "ok"));
        assertThat(Html.card(new String("<b>News</b>"), new String("ok"))).isEqualTo(card("&lt;b&gt;News&lt;/b&gt;", "ok"));
    }

    @Test
    void anAmpersandIsEscapedFirst() {
        assertThat(Html.card(new String("Hi"), new String("Tom & Jerry <3"))).isEqualTo(card("Hi", "Tom &amp; Jerry &lt;3"));
        assertThat(Html.card(new String("a > b"), new String("&"))).isEqualTo(card("a &gt; b", "&amp;"));
        assertThat(Html.card(new String("Hi"), new String("&lt;"))).isEqualTo(card("Hi", "&amp;lt;"));
    }

    @Test
    void doubleQuotesAreEscaped() {
        assertThat(Html.card(new String("Hi"), new String("Say \"hi\""))).isEqualTo(card("Hi", "Say &quot;hi&quot;"));
    }
}
