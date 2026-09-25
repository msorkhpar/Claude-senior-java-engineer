package practice;

import java.util.ArrayList;
import java.util.List;

public class Playlist {

    /** A song whose title can change. */
    public static class Song {
        private String title;

        public Song(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    private final String name;
    private final List<Song> songs;

    public Playlist(String name, List<Song> songs) {
        this.name = name;
        this.songs = songs;
    }

    public String getName() {
        return name;
    }

    public List<Song> getSongs() {
        return songs;
    }

    /** A new playlist that SHARES this playlist's list of songs. */
    public Playlist shallowCopy() {
        return new Playlist(name, new ArrayList<>(songs));
    }

    /** A new playlist with its own list, holding its own copy of every song. */
    public Playlist deepCopy() {
        List<Song> copied = new ArrayList<>();
        for (Song song : songs) {
            copied.add(new Song(song.getTitle()));
        }
        return new Playlist(name, copied);
    }
}
