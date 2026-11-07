package practice;

import java.util.Iterator;

/**
 * Decides what the payments client does with each HTTP status it gets back, and drives the
 * attempts of one request from those decisions.
 */
public class RetryPolicy {

    public enum Action { DONE, RETRY_LATER, RETRY_NOW, GIVE_UP, UNEXPECTED }

    /**
     * Sends one request until a response ends it or {@code maxAttempts} is reached, and returns
     * the number of attempts made. {@code responses} yields the status of each attempt in turn.
     */
    public int attempts(Iterator<Integer> responses, int maxAttempts) {
        int made = 0;
        while (made < maxAttempts && responses.hasNext()) {
            made++;
            Action action = decide(responses.next());
            if (action != Action.RETRY_LATER && action != Action.RETRY_NOW) {
                break;
            }
        }
        return made;
    }

    /** What the client does with one response status. */
    Action decide(int status) {
        // TODO: Return the action for this status, as the policy in the statement lists it.
        //       Every int can arrive here, not only the codes the policy names.
        throw new IllegalStateException("TODO: decide");
    }
}
