package practice;

public final class CommentRenderer {

    private CommentRenderer() {
    }

    /** HTML-encodes text for rendering; null gives "". */
    public static String encode(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }

    /** Renders a user comment inside the page's comment div. */
    public static String render(String comment) {
        return "<div class='comment'>" + encode(comment) + "</div>";
    }
}
