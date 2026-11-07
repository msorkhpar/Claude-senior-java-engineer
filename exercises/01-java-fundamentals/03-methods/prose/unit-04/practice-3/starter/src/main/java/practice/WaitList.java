package practice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The wait list of a repair shop: customers take a numbered ticket and are served in the order
 * they joined. An account can also have a hold on it, with the reason, until someone releases it.
 */
public class WaitList {

    private final List<Integer> tickets = new ArrayList<>();
    private final Map<Long, String> holds = new HashMap<>();

    public void join(int ticket) {
        tickets.add(ticket);
    }

    /** The tickets still waiting, in the order they joined. */
    public List<Integer> waiting() {
        return List.copyOf(tickets);
    }

    public void hold(long accountId, String reason) {
        holds.put(accountId, reason);
    }

    /** The accounts on hold, with their reasons. */
    public Map<Long, String> holds() {
        return Map.copyOf(holds);
    }

    /** The customer holding this ticket number left: take the ticket off the list. */
    public void cancel(int ticket) {
        // TODO: remove this ticket number from the list; tickets behind it move up.
        throw new IllegalStateException("TODO: cancel");
    }

    /** The hold on this account is lifted. */
    public void release(int accountId) {
        // TODO: lift the hold on this account. Account ids reach this method as an int.
        throw new IllegalStateException("TODO: release");
    }
}
