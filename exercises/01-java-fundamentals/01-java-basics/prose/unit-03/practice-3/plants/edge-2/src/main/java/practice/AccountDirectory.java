package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Finds the owner of an account. Accounts are kept by a 64-bit id. Events arrive decoded into a
 * {@code Map<String, Object>}; the decoder gives a number as an Integer when it fits in an int and
 * as a Long when it doesn't. Old card terminals send a 32-bit id of their own.
 */
public class AccountDirectory {

    private final Map<Long, String> owners = new HashMap<>();

    public void open(long accountId, String owner) {
        owners.put(accountId, owner);
    }

    /** The owner of the account named by the event's "accountId" field. */
    public Optional<String> ownerOf(Map<String, Object> event) {
        return Optional.ofNullable(owners.get(accountId(event)));
    }

    /** The account id carried in the event's "accountId" field. */
    long accountId(Map<String, Object> event) {
        // TODO: Read the id, whichever of the decoder's types it arrived as.
        return ((Number) event.get("accountId")).intValue();
    }

    /** Whether an old terminal's account id names an open account. */
    boolean isOpen(int terminalAccountId) {
        // TODO: Answer from the accounts opened so far.
        return owners.containsKey((long) terminalAccountId);
    }
}
