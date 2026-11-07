package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PrefetchTest {

    private static final Map<String, String> HANDLES = Map.of(
            "u-71", "mira", "u-08", "tobi", "u-33", "zed");

    private final Prefetch prefetch = new Prefetch(id -> Optional.ofNullable(HANDLES.get(id)));

    private static Prefetch.Playlist list(String name, String owner, String... tracks) {
        return new Prefetch.Playlist(name, owner, List.of(tracks));
    }

    @Test
    void theBannerCountsTracksAndNamesEachOwnerOnce() {
        List<Prefetch.Playlist> home = List.of(
                list("Night Drive", "u-08", "t-901"),
                list("Focus", "u-71", "t-344"),
                list("Rainy Sunday", "u-08", "t-120"));

        assertThat(prefetch.banner(home)).isEqualTo("3 tracks from @tobi, @mira");
    }

    @Test
    void anEmptyHomeScreenFetchesNothing() {
        assertThat(prefetch.trackCount(List.of())).isZero();
        assertThat(prefetch.ownerHandles(List.of())).isEmpty();
    }

    @Test
    void everyTrackOfEveryPlaylistIsCounted() {
        List<Prefetch.Playlist> home = List.of(
                list("Workout", "u-33", "t-1", "t-2", "t-3", "t-4"),
                list("New Releases", "u-71"),
                list("Throwbacks", "u-08", "t-5", "t-1"));

        assertThat(prefetch.trackCount(home)).isEqualTo(6);
    }

    @Test
    void anOwnerWhoseAccountIsGoneIsLeftOut() {
        List<Prefetch.Playlist> home = List.of(
                list("Old Mix", "u-404", "t-77"),
                list("Focus", "u-71", "t-344"),
                list("Lost Tapes", "u-405", "t-78"));

        assertThat(prefetch.ownerHandles(home)).containsExactly("mira");
        assertThat(prefetch.banner(home)).isEqualTo("3 tracks from @mira");
    }
}
