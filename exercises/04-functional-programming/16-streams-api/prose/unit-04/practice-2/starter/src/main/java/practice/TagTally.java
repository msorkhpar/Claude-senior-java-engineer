package practice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Counts how often each tag appears across a shop's product listings. A large catalogue is
 * tallied on a parallel stream.
 */
public class TagTally {

    /** Catalogues with at least this many tags are tallied in parallel. */
    static final int PARALLEL_FROM = 1_000;

    /** A count per tag. Not thread-safe: only one thread at a time may change a Counts. */
    public static final class Counts {
        private final Map<String, Integer> byTag = new HashMap<>();

        /** Counts one more occurrence of the tag. */
        public void add(String tag) {
            byTag.merge(tag, 1, Integer::sum);
        }

        /** Adds every count in other to this one; other is left as it was. */
        public void addAll(Counts other) {
            // TODO: Add other's counts to this one. A tag both of them have counted must end up
            //       with both counts.
            throw new IllegalStateException("TODO: addAll");
        }

        public int of(String tag) {
            return byTag.getOrDefault(tag, 0);
        }

        public Map<String, Integer> asMap() {
            return Map.copyOf(byTag);
        }
    }

    /** Tallies every tag of the catalogue. */
    public static Counts tally(List<String> tags) {
        return tally(tags.size() >= PARALLEL_FROM ? tags.parallelStream() : tags.stream());
    }

    static Counts tally(Stream<String> tags) {
        // TODO: Count every tag of the stream into a Counts. The stream may be sequential or
        //       parallel, and the answer must be the same.
        throw new IllegalStateException("TODO: tally");
    }
}
