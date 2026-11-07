package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

class PlaylistTest {

    private static Playlist withThree() {
        Playlist p = new Playlist();
        p.add("Gimme! Gimme! Gimme!");
        p.add("September");
        p.add("Le Freak");
        return p;
    }

    private static int statusOf(Runnable call) {
        Throwable t = catchThrowable(call::run);
        assertThat(t).as("the call should fail with an exception the API can answer").isInstanceOf(RuntimeException.class);
        return Playlist.Api.status((RuntimeException) t);
    }

    @Test
    void aMissingPositionCanBeCaughtByTheMobileAppWithThePositionAsked() {
        Playlist p = withThree();

        assertThatThrownBy(() -> p.track(7))
                .isInstanceOf(Playlist.NoSuchTrackException.class)
                .satisfies(e -> assertThat(((Playlist.NoSuchTrackException) e).position()).isEqualTo(7));
        assertThat(p.track(3)).isEqualTo("Le Freak");
    }

    @Test
    void aLockedPlaylistRefusesTracksUntilItIsUnlocked() {
        Playlist p = withThree();
        p.lock();

        assertThatThrownBy(() -> p.add("Dancing Queen")).isInstanceOf(RuntimeException.class);
        p.unlock();
        p.add("Dancing Queen");
        assertThat(p.track(4)).isEqualTo("Dancing Queen");
    }

    @Test
    void theScreenViewNeverChangesThePlaylist() {
        Playlist p = withThree();
        Playlist screen = p.screenView();

        assertThatThrownBy(() -> screen.add("Disco Inferno")).isInstanceOf(RuntimeException.class);
        assertThat(screen.track(2)).isEqualTo("September");
        assertThatThrownBy(() -> p.track(4)).isInstanceOf(RuntimeException.class);
    }

    @Test
    void aMissingPositionTellsTheClientItsRequestIsWrong() {
        Playlist p = withThree();

        assertThat(statusOf(() -> p.track(0))).isEqualTo(400);
        assertThat(statusOf(() -> p.track(4))).isEqualTo(400);
    }

    @Test
    void aTrackAddedWhileLockedTellsTheClientToTryAgainLater() {
        Playlist p = withThree();
        p.lock();

        assertThat(statusOf(() -> p.add("Dancing Queen"))).isEqualTo(409);
    }

    @Test
    void anAddThroughTheScreenViewTellsTheClientItIsNeverAllowed() {
        Playlist screen = withThree().screenView();

        assertThat(statusOf(() -> screen.add("Disco Inferno"))).isEqualTo(405);
    }
}
