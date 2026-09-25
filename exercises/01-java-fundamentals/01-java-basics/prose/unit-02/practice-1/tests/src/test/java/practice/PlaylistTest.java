package practice;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlaylistTest {

    private static Playlist original() {
        List<Playlist.Song> songs = new ArrayList<>();
        songs.add(new Playlist.Song("Intro"));
        songs.add(new Playlist.Song("Outro"));
        return new Playlist("Mix", songs);
    }

    private static List<String> titles(Playlist playlist) {
        return playlist.getSongs().stream().map(Playlist.Song::getTitle).toList();
    }

    @Test
    void copiesCarryTheSameContent() {
        Playlist original = original();
        Playlist shallow = original.shallowCopy();
        Playlist deep = original.deepCopy();
        assertThat(shallow).isNotSameAs(original);
        assertThat(deep).isNotSameAs(original);
        assertThat(shallow.getName()).isEqualTo("Mix");
        assertThat(deep.getName()).isEqualTo("Mix");
        assertThat(titles(shallow)).containsExactly("Intro", "Outro");
        assertThat(titles(deep)).containsExactly("Intro", "Outro");
    }

    @Test
    void aShallowCopySharesTheList() {
        Playlist original = original();
        Playlist shallow = original.shallowCopy();
        original.getSongs().add(new Playlist.Song("Encore"));
        assertThat(shallow.getSongs()).isSameAs(original.getSongs());
        assertThat(titles(shallow)).containsExactly("Intro", "Outro", "Encore");
    }

    @Test
    void aDeepCopyOwnsItsList() {
        Playlist original = original();
        Playlist deep = original.deepCopy();
        original.getSongs().add(new Playlist.Song("Encore"));
        assertThat(deep.getSongs()).isNotSameAs(original.getSongs());
        assertThat(titles(deep)).containsExactly("Intro", "Outro");
    }

    @Test
    void aDeepCopyOwnsItsSongs() {
        Playlist original = original();
        Playlist deep = original.deepCopy();
        original.getSongs().get(0).setTitle("Renamed");
        assertThat(titles(deep)).containsExactly("Intro", "Outro");
        assertThat(deep.getSongs().get(0)).isNotSameAs(original.getSongs().get(0));
    }
}
