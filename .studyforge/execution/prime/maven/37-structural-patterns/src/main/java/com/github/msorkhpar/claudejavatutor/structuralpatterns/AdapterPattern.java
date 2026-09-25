package com.github.msorkhpar.claudejavatutor.structuralpatterns;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Demonstrates the Adapter Pattern — a structural design pattern that allows objects with
 * incompatible interfaces to collaborate. The adapter acts as a bridge between two
 * incompatible interfaces.
 */
public class AdapterPattern {

    // -----------------------------------------------------------------------
    // Target interface — the interface the client expects
    // -----------------------------------------------------------------------

    /**
     * Modern media player interface that clients program against.
     */
    public interface MediaPlayer {
        String play(String filename);
        String getPlayerType();
    }

    // -----------------------------------------------------------------------
    // Adaptee classes — existing classes with incompatible interfaces
    // -----------------------------------------------------------------------

    /**
     * Legacy audio player that uses a different interface contract.
     */
    public static class LegacyAudioPlayer {
        public String playMp3(String filename) {
            Objects.requireNonNull(filename, "Filename must not be null");
            if (filename.isBlank()) {
                throw new IllegalArgumentException("Filename must not be blank");
            }
            return "Playing MP3: " + filename;
        }
    }

    /**
     * Third-party video player with yet another interface.
     */
    public static class ThirdPartyVideoPlayer {
        public String renderVideo(String path, int width, int height) {
            Objects.requireNonNull(path, "Path must not be null");
            if (path.isBlank()) {
                throw new IllegalArgumentException("Path must not be blank");
            }
            return "Rendering video (%dx%d): %s".formatted(width, height, path);
        }
    }

    // -----------------------------------------------------------------------
    // Object Adapter — uses composition (preferred approach)
    // -----------------------------------------------------------------------

    /**
     * Object adapter that adapts LegacyAudioPlayer to the MediaPlayer interface
     * using composition (wrapping the adaptee).
     */
    public static class AudioPlayerAdapter implements MediaPlayer {

        private final LegacyAudioPlayer legacyPlayer;

        public AudioPlayerAdapter(LegacyAudioPlayer legacyPlayer) {
            this.legacyPlayer = Objects.requireNonNull(legacyPlayer, "Legacy player must not be null");
        }

        @Override
        public String play(String filename) {
            return legacyPlayer.playMp3(filename);
        }

        @Override
        public String getPlayerType() {
            return "Audio (Object Adapter)";
        }
    }

    /**
     * Object adapter that adapts ThirdPartyVideoPlayer to the MediaPlayer interface
     * with default resolution settings.
     */
    public static class VideoPlayerAdapter implements MediaPlayer {

        private final ThirdPartyVideoPlayer videoPlayer;
        private final int width;
        private final int height;

        public VideoPlayerAdapter(ThirdPartyVideoPlayer videoPlayer) {
            this(videoPlayer, 1920, 1080);
        }

        public VideoPlayerAdapter(ThirdPartyVideoPlayer videoPlayer, int width, int height) {
            this.videoPlayer = Objects.requireNonNull(videoPlayer, "Video player must not be null");
            if (width <= 0 || height <= 0) {
                throw new IllegalArgumentException("Width and height must be positive");
            }
            this.width = width;
            this.height = height;
        }

        @Override
        public String play(String filename) {
            return videoPlayer.renderVideo(filename, width, height);
        }

        @Override
        public String getPlayerType() {
            return "Video (Object Adapter)";
        }
    }

    // -----------------------------------------------------------------------
    // Class Adapter — uses inheritance (Java single inheritance limits this)
    // -----------------------------------------------------------------------

    /**
     * Class adapter that extends LegacyAudioPlayer and implements MediaPlayer.
     * This approach uses inheritance, which Java limits to single inheritance.
     */
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

    // -----------------------------------------------------------------------
    // Two-way Adapter — implements both interfaces
    // -----------------------------------------------------------------------

    /**
     * Interface representing an advanced streaming service.
     */
    public interface StreamingService {
        String stream(String url);
    }

    /**
     * Two-way adapter that can be used as both a MediaPlayer and a StreamingService.
     */
    public static class TwoWayMediaAdapter implements MediaPlayer, StreamingService {

        private final LegacyAudioPlayer audioPlayer;

        public TwoWayMediaAdapter(LegacyAudioPlayer audioPlayer) {
            this.audioPlayer = Objects.requireNonNull(audioPlayer, "Audio player must not be null");
        }

        @Override
        public String play(String filename) {
            return audioPlayer.playMp3(filename);
        }

        @Override
        public String getPlayerType() {
            return "Two-Way Adapter";
        }

        @Override
        public String stream(String url) {
            return "Streaming from: " + url + " via " + audioPlayer.playMp3(url);
        }
    }

    // -----------------------------------------------------------------------
    // Real-world example: adapting data formats
    // -----------------------------------------------------------------------

    /**
     * Legacy user record from an old system (uses array-based data).
     */
    public record LegacyUser(String[] data) {
        /**
         * data[0] = first name, data[1] = last name, data[2] = email
         */
        public LegacyUser {
            Objects.requireNonNull(data, "Data array must not be null");
            if (data.length < 3) {
                throw new IllegalArgumentException("Data array must have at least 3 elements");
            }
        }
    }

    /**
     * Modern user record that the new system expects.
     */
    public record ModernUser(String fullName, String email) {
        public ModernUser {
            Objects.requireNonNull(fullName, "Full name must not be null");
            Objects.requireNonNull(email, "Email must not be null");
        }
    }

    /**
     * Adapter that converts legacy user data to modern user format.
     */
    public static class UserDataAdapter {

        public ModernUser adapt(LegacyUser legacyUser) {
            Objects.requireNonNull(legacyUser, "Legacy user must not be null");
            String[] data = legacyUser.data();
            String fullName = data[0] + " " + data[1];
            return new ModernUser(fullName, data[2]);
        }

        public List<ModernUser> adaptAll(List<LegacyUser> legacyUsers) {
            Objects.requireNonNull(legacyUsers, "Legacy users list must not be null");
            return legacyUsers.stream()
                    .map(this::adapt)
                    .collect(Collectors.toList());
        }
    }
}
