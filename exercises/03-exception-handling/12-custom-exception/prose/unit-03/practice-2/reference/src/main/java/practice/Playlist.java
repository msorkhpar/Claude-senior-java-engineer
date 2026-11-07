package practice;

import java.util.ArrayList;
import java.util.List;

/**
 * A party playlist that guests add tracks to. The host locks it while the DJ reorders it, and
 * the venue's screens get a view of it that only displays.
 */
public class Playlist {

    /** The API's error handler, shared by every service. Don't change it. */
    public static final class Api {
        private Api() {
        }

        public static int status(RuntimeException e) {
            if (e instanceof IllegalArgumentException) return 400;
            if (e instanceof IllegalStateException) return 409;
            if (e instanceof UnsupportedOperationException) return 405;
            return 500;
        }
    }

    /** The mobile app catches this one to offer a search instead of an error. */
    public static class NoSuchTrackException extends IllegalArgumentException {
        private final int position;

        public NoSuchTrackException(int position, int size) {
            super("position " + position + " outside [1, " + size + "]");
            this.position = position;
        }

        public int position() {
            return position;
        }
    }

    private final List<String> tracks = new ArrayList<>();
    private boolean locked;

    /** The track at a position counted from 1. */
    public String track(int position) {
        if (position < 1 || position > tracks.size()) throw noSuchTrack(position);
        return tracks.get(position - 1);
    }

    public void add(String track) {
        if (locked) throw lockedForReordering();
        tracks.add(track);
    }

    public void lock() {
        locked = true;
    }

    public void unlock() {
        locked = false;
    }

    /** What the venue's screens get: they display the playlist and never change it. */
    public Playlist screenView() {
        Playlist owner = this;
        return new Playlist() {
            @Override
            public String track(int position) {
                return owner.track(position);
            }

            @Override
            public void add(String track) {
                throw displayOnly();
            }
        };
    }

    RuntimeException noSuchTrack(int position) {
        return new NoSuchTrackException(position, tracks.size());
    }

    RuntimeException lockedForReordering() {
        return new IllegalStateException("playlist is locked for reordering");
    }

    RuntimeException displayOnly() {
        return new UnsupportedOperationException("screen view is display-only");
    }
}
