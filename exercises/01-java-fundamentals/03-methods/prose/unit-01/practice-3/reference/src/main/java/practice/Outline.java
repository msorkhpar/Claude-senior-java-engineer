package practice;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** The outline view of a discussion: one line per post, showing how deep in the thread it sits. */
public class Outline {

    /** A post in a discussion; the replies to it, oldest first. */
    public static final class Post {
        final String author;
        final List<Post> replies = new ArrayList<>();

        public Post(String author) {
            this.author = author;
        }

        /** Adds a reply to this post and returns it. */
        public Post reply(String author) {
            Post reply = new Post(author);
            replies.add(reply);
            return reply;
        }

        @Override
        public String toString() {
            return "Post(" + author + ", " + replies.size() + " replies)";
        }
    }

    /**
     * One line per post, "depth author": the opening post is "0 ann", a reply to it "1 bob", a
     * reply to that "2 ...". Lines are in reading order: a post, then everything under it, then
     * the post's next sibling. Some discussions are hundreds of thousands of replies deep.
     */
    public static List<String> lines(Post opening) {
        List<String> lines = new ArrayList<>();
        Deque<Pending> stack = newStack();
        stack.push(new Pending(opening, 0));
        while (!stack.isEmpty()) {
            Pending p = stack.pop();
            lines.add(p.depth() + " " + p.post().author);
            List<Post> replies = p.post().replies;
            for (int i = replies.size() - 1; i >= 0; i--) stack.push(new Pending(replies.get(i), p.depth() + 1));
        }
        return lines;
    }

    /** A post waiting to be written out, with its depth. Use it if it helps. */
    record Pending(Post post, int depth) {}

    static Deque<Pending> newStack() {
        return new ArrayDeque<>();
    }
}
