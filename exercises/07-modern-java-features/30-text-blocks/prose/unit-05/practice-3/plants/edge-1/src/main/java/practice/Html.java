package practice;

public final class Html {

    private Html() {
    }

    /** Returns an HTML card showing {@code title} and {@code body}, both escaped. */
    public static String card(String title, String body) {
        return """
                <div class="card">
                    <h1>%s</h1>
                    <p>%s</p>
                </div>""".formatted(title, escape(body));
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
