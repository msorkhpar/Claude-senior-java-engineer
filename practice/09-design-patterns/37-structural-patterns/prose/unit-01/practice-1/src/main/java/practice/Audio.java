package practice;

import java.util.Objects;

public final class Audio {

    private Audio() {
    }

    /** The interface clients program against. */
    public interface MediaPlayer {
        String play(String filename);

        String getPlayerType();
    }

    /** The existing player, with a method of its own name and shape. */
    public static class LegacyAudioPlayer {
        public String playMp3(String filename) {
            Objects.requireNonNull(filename, "filename must not be null");
            if (filename.isBlank()) {
                throw new IllegalArgumentException("filename must not be blank");
            }
            return "Playing MP3: " + filename;
        }
    }

    /** Object adapter: wraps a player. */
    public static final class AudioPlayerAdapter implements MediaPlayer {

        public AudioPlayerAdapter(LegacyAudioPlayer player) {
            throw new UnsupportedOperationException("write the constructor");
        }

        @Override
        public String play(String filename) {
            throw new UnsupportedOperationException("write play");
        }

        @Override
        public String getPlayerType() {
            throw new UnsupportedOperationException("write getPlayerType");
        }
    }

    /** Class adapter: is a player. */
    public static class AudioClassAdapter extends LegacyAudioPlayer implements MediaPlayer {

        @Override
        public String play(String filename) {
            throw new UnsupportedOperationException("write play");
        }

        @Override
        public String getPlayerType() {
            throw new UnsupportedOperationException("write getPlayerType");
        }
    }
}
