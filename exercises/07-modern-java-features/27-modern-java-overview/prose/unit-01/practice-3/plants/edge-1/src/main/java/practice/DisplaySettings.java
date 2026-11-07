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
    private Optional<String> timeZone = Optional.empty();

    public DisplaySettings(String user) {
        this.user = user;
    }

    public String user() {
        return user;
    }

    /** Sets the user's preferred time zone, such as "Europe/Lisbon"; null clears it. */
    public void setTimeZone(String zoneId) {
        this.timeZone = Optional.ofNullable(zoneId);
    }

    /** The user's preferred time zone, if they picked one. */
    public Optional<String> timeZone() {
        return timeZone;
    }
}
