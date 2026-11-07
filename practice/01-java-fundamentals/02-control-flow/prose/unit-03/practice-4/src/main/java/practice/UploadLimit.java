package practice;

import java.util.function.IntPredicate;

/**
 * Finds the largest upload, in bytes, that a storage gateway accepts. Each probe sends a request
 * of that size to a test endpoint, so the search must use few probes. A gateway that accepts a
 * size accepts every smaller size too.
 */
public class UploadLimit {

    /** The text the client shows next to its upload button. */
    public static String hint(int maxBytes, IntPredicate accepts) {
        int largest = largestAccepted(maxBytes, accepts);
        return largest == 0 ? "uploads are disabled" : "files up to " + largest + " bytes";
    }

    /**
     * The largest size in 1..maxBytes that accepts allows, or 0 when it refuses even 1 byte.
     * Asks accepts only about sizes in 1..maxBytes.
     */
    static int largestAccepted(int maxBytes, IntPredicate accepts) {
        // TODO: Search 1..maxBytes for the largest size the gateway accepts, with as few probes as
        //       the range allows. Probe only sizes inside the range; maxBytes can be as large as
        //       an int gets.
        throw new IllegalStateException("TODO: largestAccepted");
    }
}
