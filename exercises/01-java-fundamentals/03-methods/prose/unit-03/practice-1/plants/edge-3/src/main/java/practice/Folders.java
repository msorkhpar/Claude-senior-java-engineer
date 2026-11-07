package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * The mail client's folders. Each folder is a list of messages that the client's screens hold
 * and redraw from; the folder operations below change those lists.
 */
public class Folders {

    public record Message(String from, String subject) { }

    /** Deletes every message of the folder from the given sender, and returns how many it deleted. */
    public static int deleteFrom(String sender, List<Message> folder) {
        int before = folder.size();
        folder.removeIf(m -> m.from().equals(sender));
        return before - folder.size();
    }

    /** A new list of the folder's messages whose subject contains the text. */
    public static List<Message> search(List<Message> folder, String text) {
        List<Message> found = new ArrayList<>();
        for (Message m : folder) {
            if (m.subject().contains(text)) found.add(m);
        }
        return found;
    }

    /**
     * "Archive all": moves every message of the inbox to the end of the archive, in the order
     * they were in, and returns how many it moved.
     */
    public static int archiveAll(List<Message> inbox, List<Message> archive) {
        int moved = inbox.size();
        if (archive.isEmpty()) archive = new ArrayList<>();
        archive.addAll(inbox);
        inbox.clear();
        return moved;
    }
}
