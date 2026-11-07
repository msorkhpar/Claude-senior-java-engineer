package practice;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

/**
 * The "Open Recent" menu of a text editor: the files the user opened, at most {@code capacity}
 * of them. The editor shows the menu most recent first and reopens the most recent file on startup.
 */
public class RecentFiles {

    private final int capacity;
    private final LinkedHashSet<String> files = new LinkedHashSet<>();   // oldest first, newest last

    public RecentFiles(int capacity) {
        if (capacity < 1) throw new IllegalArgumentException("capacity must be at least 1: " + capacity);
        this.capacity = capacity;
    }

    /** The menu, most recent first: a copy the caller may keep. */
    public List<String> menu() {
        return List.copyOf(files.reversed());
    }

    /**
     * Records that the user opened a file. The file becomes the most recent one, and the menu
     * never holds more than {@code capacity} files, dropping the one opened longest ago.
     */
    public void open(String path) {
        // TODO: The opened file becomes the most recent one; the menu holds at most capacity
        //       files and drops the one opened longest ago.
        throw new IllegalStateException("TODO: record the opened file");
    }

    /** The file to reopen on startup, or empty when the user has never opened one. */
    public Optional<String> mostRecent() {
        // TODO: The most recent file, or empty on a fresh install.
        throw new IllegalStateException("TODO: return the most recent file");
    }
}
