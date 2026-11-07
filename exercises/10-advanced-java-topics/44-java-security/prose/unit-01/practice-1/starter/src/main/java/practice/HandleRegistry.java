package practice;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Registers the public handles members choose at sign-up. A handle is shown on the member's
 * profile, and the mention service delivers "@handle" mentions by looking the handle up after
 * NFKC normalisation.
 */
public class HandleRegistry {

    private static final Set<String> RESERVED = Set.of("admin", "support", "security", "root");

    private final Set<String> taken = new HashSet<>();

    /** Registers raw as a handle and returns the handle as stored; refuses with IllegalArgumentException. */
    public String register(String raw) {
        if (raw == null) throw new IllegalArgumentException("no handle");
        String handle = canonical(raw);
        if (!allowed(handle)) throw new IllegalArgumentException("handle not allowed");
        if (RESERVED.contains(handle)) throw new IllegalArgumentException("handle reserved");
        if (!taken.add(handle)) throw new IllegalArgumentException("handle taken");
        return handle;
    }

    /** The form of raw that this registry checks, stores and compares. */
    String canonical(String raw) {
        // TODO: Return the form of the handle that the rest of this class works with. Two
        //       spellings that the mention service treats as one handle must come out the same.
        throw new IllegalStateException("TODO: canonical");
    }

    /** Whether handle keeps to the rules for handles. */
    boolean allowed(String handle) {
        // TODO: Answer whether the handle keeps to the rules in the statement, all of it.
        throw new IllegalStateException("TODO: allowed");
    }
}
