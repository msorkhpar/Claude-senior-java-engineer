package practice;

import java.util.HashMap;
import java.util.Map;

/**
 * Enrollments in the conference's workshops. Attendees arrive from two places: the sign-up page,
 * and the import of badges sold by the partner ticketing system. Both run on many threads at once.
 */
public class Workshops {

    /** The venue's rule: whether a workshop admits one more attendee, given how many are enrolled. */
    public interface Capacity {
        boolean admits(String workshop, int enrolledSoFar);
    }

    private final Capacity capacity;
    private final Map<String, Integer> enrolled = new HashMap<>();   // guarded by this

    public Workshops(Capacity capacity) {
        this.capacity = capacity;
    }

    /** How many attendees the workshop has now. */
    public synchronized int enrolled(String workshop) {
        return enrolled.getOrDefault(workshop, 0);
    }

    /** Whether the workshop has room for one more right now, by the venue's rule. */
    public boolean hasRoom(String workshop) {
        return capacity.admits(workshop, enrolled(workshop));
    }

    /** Staff at the door may always add a walk-in, over capacity if they choose. */
    public synchronized void addWalkIn(String workshop) {
        enrolled.merge(workshop, 1, Integer::sum);
    }

    /**
     * Enrolls one attendee if the venue's rule admits one more. True if this call took a seat.
     * Called from the sign-up page and the badge import, on many threads at once.
     */
    public synchronized boolean tryEnroll(String workshop) {
        int now = enrolled.getOrDefault(workshop, 0);
        if (!capacity.admits(workshop, now)) return false;
        enrolled.put(workshop, now + 1);
        return true;
    }

    /** The sign-up page. */
    public static class SignupPage {
        private final Workshops workshops;

        public SignupPage(Workshops workshops) {
            this.workshops = workshops;
        }

        /** True if the attendee got a seat; the page shows the wait list otherwise. */
        public boolean signUp(String workshop, String email) {
            return workshops.tryEnroll(workshop);
        }
    }

    /** The import of badges sold by the partner ticketing system, one badge per call. */
    public static class BadgeImport {
        private final Workshops workshops;

        public BadgeImport(Workshops workshops) {
            this.workshops = workshops;
        }

        /** True if the badge holder got a seat; the import puts them on the wait list otherwise. */
        public boolean importBadge(String workshop, String badgeId) {
            return workshops.tryEnroll(workshop);
        }
    }
}
