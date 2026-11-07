package practice;

import java.io.Serializable;
import java.util.Optional;

/**
 * A user's display settings. The web tier keeps them in the HTTP session, and the cluster
 * replicates sessions between nodes by serializing them.
 */
public class DisplaySettings implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String user;

    public DisplaySettings(String user) {
        this.user = user;
    }

    public String user() {
        return user;
    }

    /** Sets the user's preferred time zone, such as "Europe/Lisbon"; null clears it. */
    public void setTimeZone(String zoneId) {
        // TODO: Remember the user's choice, or forget it when zoneId is null. Add whatever field
        //       you need.
        throw new IllegalStateException("TODO: setTimeZone");
    }

    /** The user's preferred time zone, if they picked one. */
    public Optional<String> timeZone() {
        // TODO: Answer with the user's choice, or an empty Optional when there is none.
        throw new IllegalStateException("TODO: timeZone");
    }
}
