package practice;

/**
 * Enforces the length limit on posts. The product spec counts one character per Unicode code
 * point, so an emoji such as U+1F389 counts as one.
 */
public class PostLimit {

    private final int limit;

    public PostLimit(int limit) {
        this.limit = limit;
    }

    /** What the composer shows under the text box. */
    public String counter(String post) {
        return length(post) + "/" + limit;
    }

    /** The post as the preview shows it: cut to the limit when it is longer. */
    public String preview(String post) {
        return length(post) <= limit ? post : truncate(post, limit);
    }

    /** How many characters the post has, by the spec's count. */
    int length(String post) {
        return (int) post.codePoints().count();
    }

    /** The first max characters of the post, by the spec's count. */
    String truncate(String post, int max) {
        return post.codePoints().limit(max)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
