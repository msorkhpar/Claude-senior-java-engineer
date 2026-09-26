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
    public static final class AudioPlayerAdapter extends LegacyAudioPlayer implements MediaPlayer {

        private final LegacyAudioPlayer player;

        public AudioPlayerAdapter(LegacyAudioPlayer player) {
            this.player = Objects.requireNonNull(player, "player must not be null");
        }

        @Override
        public String play(String filename) {
            return playMp3(filename);
        }

        @Override
        public String getPlayerType() {
            return "Audio (Object Adapter)";
        }
    }

    /** Class adapter: is a player. */
    public static class AudioClassAdapter extends LegacyAudioPlayer implements MediaPlayer {

        @Override
        public String play(String filename) {
            return playMp3(filename);
        }

        @Override
        public String getPlayerType() {
            return "Audio (Class Adapter)";
        }
    }
}
