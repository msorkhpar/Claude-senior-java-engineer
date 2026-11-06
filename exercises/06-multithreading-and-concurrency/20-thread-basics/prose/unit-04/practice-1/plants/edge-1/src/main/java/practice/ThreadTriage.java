package practice;

import java.lang.management.ThreadInfo;
import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Sorts the threads of a snapshot for the service's admin page, so that on-call can tell at a
 * glance whether requests are stuck behind another thread or simply have nothing to do.
 */
public class ThreadTriage {

    public enum Activity {
        /** Doing something of its own: computing, or inside a call such as a socket read. */
        ACTIVE,
        /** Waiting for work or for time to pass, with no other thread in its way. */
        IDLE,
        /** Can't go on until another thread lets go of something it holds. */
        STUCK_BEHIND_ANOTHER
    }

    /** How many threads of the snapshot are in each activity; every activity is a key. */
    public static Map<Activity, Integer> summary(ThreadInfo[] snapshot) {
        Map<Activity, Integer> counts = new EnumMap<>(Activity.class);
        for (Activity activity : Activity.values()) counts.put(activity, 0);
        for (ThreadInfo thread : snapshot) counts.merge(classify(thread), 1, Integer::sum);
        return counts;
    }

    /** For each thread that others are stuck behind, how many are stuck behind it, by thread name. */
    public static Map<String, Integer> stuckBehind(ThreadInfo[] snapshot) {
        Map<String, Integer> behind = new TreeMap<>();
        for (ThreadInfo thread : snapshot) {
            if (classify(thread) == Activity.STUCK_BEHIND_ANOTHER) {
                behind.merge(thread.getLockOwnerName(), 1, Integer::sum);
            }
        }
        return behind;
    }

    /** The activity of one thread of a snapshot taken with ThreadMXBean. */
    static Activity classify(ThreadInfo thread) {
        if (thread.getThreadState() == Thread.State.BLOCKED) return Activity.STUCK_BEHIND_ANOTHER;
        return thread.getThreadState() == Thread.State.RUNNABLE ? Activity.ACTIVE : Activity.IDLE;
    }
}
