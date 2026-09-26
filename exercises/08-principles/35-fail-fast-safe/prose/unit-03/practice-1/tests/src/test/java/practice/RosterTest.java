package practice;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class RosterTest {

    @Test
    void bothShowTheCurrentNames() {
        Roster r = new Roster();
        r.add("ada");
        r.add("bo");
        assertThat(r.snapshot()).containsExactly("ada", "bo");
        assertThat(r.liveView()).containsExactly("ada", "bo");
    }

    @Test
    void aSnapshotKeepsItsContents() {
        Roster r = new Roster();
        r.add("ada");
        List<String> snap = r.snapshot();
        r.add("bo");
        assertThat(snap).containsExactly("ada");
        assertThat(r.snapshot()).containsExactly("ada", "bo");
    }

    @Test
    void aLiveViewFollowsTheRoster() {
        Roster r = new Roster();
        r.add("ada");
        List<String> view = r.liveView();
        r.add("bo");
        assertThat(view).containsExactly("ada", "bo");
    }

    @Test
    void neitherCanBeChanged() {
        Roster r = new Roster();
        r.add("ada");
        List<String> snap = r.snapshot();
        List<String> view = r.liveView();
        assertThatThrownBy(() -> snap.add("x")).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> snap.remove(0)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> view.add("x")).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(view::clear).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> snap.set(0, "x")).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> view.set(0, "x")).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> view.remove("ada")).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> view.remove(0)).isInstanceOf(UnsupportedOperationException.class);
        assertThat(r.liveView()).containsExactly("ada");
    }
}
