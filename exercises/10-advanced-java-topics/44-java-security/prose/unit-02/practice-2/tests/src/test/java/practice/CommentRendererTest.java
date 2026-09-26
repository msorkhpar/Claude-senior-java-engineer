package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommentRendererTest {

    @Test
    void encodesMarkupInAComment() {
        assertThat(CommentRenderer.encode("<script>alert(\"x\")</script>"))
                .isEqualTo("&lt;script&gt;alert(&quot;x&quot;)&lt;/script&gt;");
        assertThat(CommentRenderer.encode("Tom & Jerry")).isEqualTo("Tom &amp; Jerry");
        assertThat(CommentRenderer.render("<b>hi</b>"))
                .isEqualTo("<div class='comment'>&lt;b&gt;hi&lt;/b&gt;</div>");
    }

    @Test
    void aSingleQuoteCannotCloseTheAttribute() {
        assertThat(CommentRenderer.render("it's")).isEqualTo("<div class='comment'>it&#x27;s</div>");
        assertThat(CommentRenderer.encode("' onmouseover='alert(1)"))
                .isEqualTo("&#x27; onmouseover=&#x27;alert(1)");
    }

    @Test
    void everyAmpersandIsEncoded() {
        assertThat(CommentRenderer.encode("use &lt; for <")).isEqualTo("use &amp;lt; for &lt;");
        assertThat(CommentRenderer.encode("&#60;b&#62; &amp; &quot;"))
                .isEqualTo("&amp;#60;b&amp;#62; &amp;amp; &amp;quot;");
    }

    @Test
    void aMissingCommentRendersAsEmpty() {
        assertThat(CommentRenderer.encode(null)).isEmpty();
        assertThat(CommentRenderer.render(null)).isEqualTo("<div class='comment'></div>");
    }
}
