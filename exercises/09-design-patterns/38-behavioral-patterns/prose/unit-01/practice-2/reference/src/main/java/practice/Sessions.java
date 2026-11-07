package practice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/** The session list on the admin page. */
public class Sessions {

    public record Session(String id, String user, long lastSeenMillis) {}

    /**
     * The admin page's order: the most recently seen session first; sessions last seen at the same
     * millisecond by id, A to Z.
     */
    static Comparator<Session> newestFirst() {
        return Comparator.comparingLong(Session::lastSeenMillis).reversed()
                .thenComparing(Session::id);
    }

    public static List<Session> forAdminPage(Collection<Session> sessions) {
        List<Session> page = new ArrayList<>(sessions);
        page.sort(newestFirst());
        return page;
    }
}
