package practice;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * The undo history of a form editor. It remembers the user's most recent edits, up to a limit,
 * and gives them back one at a time when the user presses Undo.
 */
public class UndoHistory {

    private final Deque<String> edits = new ArrayDeque<>();
    private final int limit;

    public UndoHistory(int limit) {
        if (limit < 1) throw new IllegalArgumentException("limit must be at least 1: " + limit);
        this.limit = limit;
    }

    /** The edits the Undo menu lists, at most n of them, the one Undo would take back first. */
    public List<String> menu(int n) {
        return edits.stream().limit(n).toList();
    }

    public int size() {
        return edits.size();
    }

    /** Remembers an edit the user just made. */
    void record(String edit) {
        edits.push(edit);
        if (edits.size() > limit) edits.poll();
    }

    /** Takes back the latest edit that hasn't been undone yet. */
    Optional<String> undo() {
        return Optional.ofNullable(edits.poll());
    }
}
