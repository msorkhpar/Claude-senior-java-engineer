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
        return playlists.stream().flatMap(p -> p.trackIds().stream()).count();
    }

    /** The handles of the playlists' owners, each once, in playlist order. */
    List<String> ownerHandles(List<Playlist> playlists) {
        return playlists.stream()
                .map(p -> profiles.handleOf(p.ownerId()).orElse(null))
                .distinct()
                .toList();
    }
}
