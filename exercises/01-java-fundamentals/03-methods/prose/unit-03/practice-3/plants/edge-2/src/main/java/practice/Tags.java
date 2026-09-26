package practice;

public final class Tags {

    private Tags() {
    }

    /** Wraps the builder's content in a tag, in place, and returns the same builder. */
    public static StringBuilder wrap(StringBuilder sb, String tag) {
        String inner = sb.toString();
        if (inner.startsWith("<")) {
            return new StringBuilder("<" + tag + ">" + inner + "</" + tag + ">");
        }
        sb.insert(0, "<" + tag + ">");
        sb.append("</").append(tag).append(">");
        return sb;
    }
}
