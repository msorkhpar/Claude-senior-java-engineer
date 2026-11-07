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
        if (status == 429 || status == 503) {
            return Action.RETRY_LATER;
        } else if (status >= 200 && status <= 299) {
            return Action.DONE;
        } else if (status >= 400 && status <= 499) {
            return Action.GIVE_UP;
        } else if (status > 500 && status <= 599) {
            return Action.RETRY_NOW;
        } else {
            return Action.UNEXPECTED;
        }
    }
}
