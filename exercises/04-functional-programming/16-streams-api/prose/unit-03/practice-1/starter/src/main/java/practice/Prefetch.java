package practice;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Before a listening session starts, the player prefetches every track of the playlists on the
 * home screen, and the banner above them says how many tracks are coming and who made the lists.
 */
public class Prefetch {

    public record Playlist(String name, String ownerId, List<String> trackIds) {}

    /** Looks up a user's public handle. A deleted account has none. */
    public interface Profiles {
        Optional<String> handleOf(String userId);
    }

    private final Profiles profiles;

    public Prefetch(Profiles profiles) {
        this.profiles = profiles;
    }

    /** The banner above the home screen, such as "12 tracks from @ana, @li". */
    public String banner(List<Playlist> playlists) {
        return trackCount(playlists) + " tracks from "
                + ownerHandles(playlists).stream().map(h -> "@" + h).collect(Collectors.joining(", "));
    }

    /** How many tracks the player fetches: every track of every playlist. */
    long trackCount(List<Playlist> playlists) {
        // TODO: Count the tracks the player will fetch across all the playlists. A track that
        //       appears in two playlists is fetched twice.
        throw new IllegalStateException("TODO: trackCount");
    }

    /** The handles of the playlists' owners, each once, in playlist order. */
    List<String> ownerHandles(List<Playlist> playlists) {
        // TODO: One handle per owner, in the order their first playlist appears. An owner whose
        //       account is gone has no handle and must not show up in the banner at all.
        throw new IllegalStateException("TODO: ownerHandles");
    }
}
