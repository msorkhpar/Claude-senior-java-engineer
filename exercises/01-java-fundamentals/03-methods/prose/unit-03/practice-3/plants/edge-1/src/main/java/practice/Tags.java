package practice;

public final class Tags {

    private Tags() {
    }

    /** Wraps the builder's content in a tag, in place, and returns the same builder. */
    public static StringBuilder wrap(StringBuilder sb, String tag) {
        sb = new StringBuilder("<" + tag + ">" + sb + "</" + tag + ">");
        return sb;
    }
}
