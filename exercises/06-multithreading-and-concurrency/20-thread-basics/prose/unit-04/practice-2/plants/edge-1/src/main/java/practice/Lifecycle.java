package practice;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.Thread.State.BLOCKED;
import static java.lang.Thread.State.NEW;
import static java.lang.Thread.State.RUNNABLE;
import static java.lang.Thread.State.TERMINATED;
import static java.lang.Thread.State.TIMED_WAITING;
import static java.lang.Thread.State.WAITING;

public final class Lifecycle {

    private static final Map<Thread.State, Set<Thread.State>> NEXT = new EnumMap<>(Thread.State.class);

    static {
        NEXT.put(NEW, EnumSet.of(RUNNABLE));
        NEXT.put(RUNNABLE, EnumSet.of(BLOCKED, WAITING, TIMED_WAITING, TERMINATED));
        NEXT.put(BLOCKED, EnumSet.of(RUNNABLE, WAITING, TIMED_WAITING));
        NEXT.put(WAITING, EnumSet.of(RUNNABLE, BLOCKED));
        NEXT.put(TIMED_WAITING, EnumSet.of(RUNNABLE, BLOCKED));
        NEXT.put(TERMINATED, EnumSet.noneOf(Thread.State.class));
    }

    private Lifecycle() {
    }

    /** Whether one thread could have entered these states, in this order. */
    public static boolean isValid(List<Thread.State> states) {
        if (states.isEmpty() || states.get(0) != NEW) {
            return false;
        }
        for (int i = 1; i < states.size(); i++) {
            if (!NEXT.get(states.get(i - 1)).contains(states.get(i))) {
                return false;
            }
        }
        return true;
    }
}
