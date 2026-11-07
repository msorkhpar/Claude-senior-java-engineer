package practice;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Exports a discussion for the moderation team: every comment in the order the site shows them,
 * and how deep the longest chain of replies goes. Exports run on the worker's ordinary threads.
 */
public class Discussion {

    /** One comment and the replies to it, oldest reply first. */
    public static final class Comment {
        private final String id;
        private final List<Comment> replies = new ArrayList<>(1);

        public Comment(String id) {
            this.id = id;
        }

        /** Adds a reply to this comment and returns it. */
        public Comment reply(String replyId) {
            Comment reply = new Comment(replyId);
            replies.add(reply);
            return reply;
        }

        public String id() {
            return id;
        }

        public List<Comment> replies() {
            return Collections.unmodifiableList(replies);
        }

        @Override
        public String toString() {
            return id;
        }
    }

    /** One line for the moderation report. */
    public static String summary(Comment root) {
        return inOrder(root).size() + " comments, " + deepest(root) + " levels";
    }

    /** How many levels the longest chain of replies has, counting the root as level 1. */
    static int deepest(Comment root) {
        // TODO: Count the levels of the longest chain of replies. Some discussions are a few
        //       comments; one was 180,000 replies, each answering the one before, and the
        //       export must handle both on the worker's ordinary threads.
        throw new IllegalStateException("TODO: deepest");
    }

    /**
     * Every comment's id in the order the site shows them: a comment, then the whole thread under
     * its oldest reply, then the thread under its next reply, and so on.
     */
    static List<String> inOrder(Comment root) {
        // TODO: List the ids in the order the site shows them (see above), for discussions of
        //       any depth, on the worker's ordinary threads.
        throw new IllegalStateException("TODO: inOrder");
    }
}
