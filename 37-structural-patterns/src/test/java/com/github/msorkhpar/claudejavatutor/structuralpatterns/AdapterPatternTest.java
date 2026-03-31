package com.github.msorkhpar.claudejavatutor.structuralpatterns;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Adapter Pattern Tests")
class AdapterPatternTest {

    @Nested
    @DisplayName("Object Adapter - AudioPlayerAdapter")
    class AudioPlayerAdapterTest {

        @Test
        @DisplayName("Should adapt legacy audio player to MediaPlayer interface")
        void testPlayDelegatesToLegacyPlayer() {
            var legacy = new AdapterPattern.LegacyAudioPlayer();
            var adapter = new AdapterPattern.AudioPlayerAdapter(legacy);

            String result = adapter.play("song.mp3");

            assertThat(result).isEqualTo("Playing MP3: song.mp3");
        }

        @Test
        @DisplayName("Should return correct player type")
        void testGetPlayerType() {
            var adapter = new AdapterPattern.AudioPlayerAdapter(new AdapterPattern.LegacyAudioPlayer());

            assertThat(adapter.getPlayerType()).isEqualTo("Audio (Object Adapter)");
        }

        @Test
        @DisplayName("Should throw NullPointerException for null legacy player")
        void testNullLegacyPlayer() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new AdapterPattern.AudioPlayerAdapter(null))
                    .withMessage("Legacy player must not be null");
        }

        @Test
        @DisplayName("Should throw NullPointerException for null filename")
        void testNullFilename() {
            var adapter = new AdapterPattern.AudioPlayerAdapter(new AdapterPattern.LegacyAudioPlayer());

            assertThatNullPointerException()
                    .isThrownBy(() -> adapter.play(null));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for blank filename")
        void testBlankFilename() {
            var adapter = new AdapterPattern.AudioPlayerAdapter(new AdapterPattern.LegacyAudioPlayer());

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> adapter.play("   "));
        }

        @Test
        @DisplayName("Should implement MediaPlayer interface")
        void testImplementsMediaPlayer() {
            var adapter = new AdapterPattern.AudioPlayerAdapter(new AdapterPattern.LegacyAudioPlayer());

            assertThat(adapter).isInstanceOf(AdapterPattern.MediaPlayer.class);
        }
    }

    @Nested
    @DisplayName("Object Adapter - VideoPlayerAdapter")
    class VideoPlayerAdapterTest {

        @Test
        @DisplayName("Should adapt third-party video player with default resolution")
        void testPlayWithDefaultResolution() {
            var videoPlayer = new AdapterPattern.ThirdPartyVideoPlayer();
            var adapter = new AdapterPattern.VideoPlayerAdapter(videoPlayer);

            String result = adapter.play("movie.mp4");

            assertThat(result).isEqualTo("Rendering video (1920x1080): movie.mp4");
        }

        @Test
        @DisplayName("Should adapt third-party video player with custom resolution")
        void testPlayWithCustomResolution() {
            var videoPlayer = new AdapterPattern.ThirdPartyVideoPlayer();
            var adapter = new AdapterPattern.VideoPlayerAdapter(videoPlayer, 3840, 2160);

            String result = adapter.play("movie.mp4");

            assertThat(result).isEqualTo("Rendering video (3840x2160): movie.mp4");
        }

        @Test
        @DisplayName("Should return correct player type")
        void testGetPlayerType() {
            var adapter = new AdapterPattern.VideoPlayerAdapter(new AdapterPattern.ThirdPartyVideoPlayer());

            assertThat(adapter.getPlayerType()).isEqualTo("Video (Object Adapter)");
        }

        @Test
        @DisplayName("Should throw NullPointerException for null video player")
        void testNullVideoPlayer() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new AdapterPattern.VideoPlayerAdapter(null))
                    .withMessage("Video player must not be null");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for non-positive dimensions")
        void testInvalidDimensions() {
            var videoPlayer = new AdapterPattern.ThirdPartyVideoPlayer();

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new AdapterPattern.VideoPlayerAdapter(videoPlayer, 0, 1080));
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new AdapterPattern.VideoPlayerAdapter(videoPlayer, 1920, -1));
        }

        @Test
        @DisplayName("Should throw NullPointerException for null filename on play")
        void testNullFilenameOnPlay() {
            var adapter = new AdapterPattern.VideoPlayerAdapter(new AdapterPattern.ThirdPartyVideoPlayer());

            assertThatNullPointerException()
                    .isThrownBy(() -> adapter.play(null));
        }
    }

    @Nested
    @DisplayName("Class Adapter - AudioClassAdapter")
    class AudioClassAdapterTest {

        @Test
        @DisplayName("Should adapt via inheritance")
        void testPlayViaInheritance() {
            var adapter = new AdapterPattern.AudioClassAdapter();

            String result = adapter.play("track.mp3");

            assertThat(result).isEqualTo("Playing MP3: track.mp3");
        }

        @Test
        @DisplayName("Should return correct player type")
        void testGetPlayerType() {
            var adapter = new AdapterPattern.AudioClassAdapter();

            assertThat(adapter.getPlayerType()).isEqualTo("Audio (Class Adapter)");
        }

        @Test
        @DisplayName("Should still have access to legacy methods directly")
        void testLegacyMethodAccess() {
            var adapter = new AdapterPattern.AudioClassAdapter();

            // Can also call the legacy method directly since it extends the adaptee
            String legacyResult = adapter.playMp3("track.mp3");

            assertThat(legacyResult).isEqualTo("Playing MP3: track.mp3");
        }

        @Test
        @DisplayName("Should be both MediaPlayer and LegacyAudioPlayer")
        void testDualType() {
            var adapter = new AdapterPattern.AudioClassAdapter();

            assertThat(adapter).isInstanceOf(AdapterPattern.MediaPlayer.class);
            assertThat(adapter).isInstanceOf(AdapterPattern.LegacyAudioPlayer.class);
        }
    }

    @Nested
    @DisplayName("Two-Way Adapter")
    class TwoWayMediaAdapterTest {

        @Test
        @DisplayName("Should work as MediaPlayer")
        void testAsMediaPlayer() {
            var adapter = new AdapterPattern.TwoWayMediaAdapter(new AdapterPattern.LegacyAudioPlayer());

            String result = adapter.play("song.mp3");

            assertThat(result).isEqualTo("Playing MP3: song.mp3");
        }

        @Test
        @DisplayName("Should work as StreamingService")
        void testAsStreamingService() {
            var adapter = new AdapterPattern.TwoWayMediaAdapter(new AdapterPattern.LegacyAudioPlayer());

            String result = adapter.stream("http://example.com/stream");

            assertThat(result).contains("Streaming from: http://example.com/stream");
            assertThat(result).contains("Playing MP3: http://example.com/stream");
        }

        @Test
        @DisplayName("Should return correct player type")
        void testGetPlayerType() {
            var adapter = new AdapterPattern.TwoWayMediaAdapter(new AdapterPattern.LegacyAudioPlayer());

            assertThat(adapter.getPlayerType()).isEqualTo("Two-Way Adapter");
        }

        @Test
        @DisplayName("Should implement both interfaces")
        void testImplementsBothInterfaces() {
            var adapter = new AdapterPattern.TwoWayMediaAdapter(new AdapterPattern.LegacyAudioPlayer());

            assertThat(adapter).isInstanceOf(AdapterPattern.MediaPlayer.class);
            assertThat(adapter).isInstanceOf(AdapterPattern.StreamingService.class);
        }
    }

    @Nested
    @DisplayName("User Data Adapter - Real-world Example")
    class UserDataAdapterTest {

        @Test
        @DisplayName("Should adapt single legacy user to modern user")
        void testAdaptSingleUser() {
            var adapter = new AdapterPattern.UserDataAdapter();
            var legacy = new AdapterPattern.LegacyUser(new String[]{"John", "Doe", "john@example.com"});

            AdapterPattern.ModernUser modern = adapter.adapt(legacy);

            assertThat(modern.fullName()).isEqualTo("John Doe");
            assertThat(modern.email()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("Should adapt list of legacy users")
        void testAdaptMultipleUsers() {
            var adapter = new AdapterPattern.UserDataAdapter();
            List<AdapterPattern.LegacyUser> legacyUsers = Arrays.asList(
                    new AdapterPattern.LegacyUser(new String[]{"John", "Doe", "john@example.com"}),
                    new AdapterPattern.LegacyUser(new String[]{"Jane", "Smith", "jane@example.com"})
            );

            List<AdapterPattern.ModernUser> modernUsers = adapter.adaptAll(legacyUsers);

            assertThat(modernUsers).hasSize(2);
            assertThat(modernUsers.get(0).fullName()).isEqualTo("John Doe");
            assertThat(modernUsers.get(1).fullName()).isEqualTo("Jane Smith");
        }

        @Test
        @DisplayName("Should handle empty list")
        void testAdaptEmptyList() {
            var adapter = new AdapterPattern.UserDataAdapter();

            List<AdapterPattern.ModernUser> result = adapter.adaptAll(Collections.emptyList());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should throw NullPointerException for null legacy user")
        void testNullLegacyUser() {
            var adapter = new AdapterPattern.UserDataAdapter();

            assertThatNullPointerException()
                    .isThrownBy(() -> adapter.adapt(null));
        }

        @Test
        @DisplayName("Should throw NullPointerException for null list")
        void testNullList() {
            var adapter = new AdapterPattern.UserDataAdapter();

            assertThatNullPointerException()
                    .isThrownBy(() -> adapter.adaptAll(null));
        }

        @Test
        @DisplayName("Should throw NullPointerException for null data array in LegacyUser")
        void testNullDataArray() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new AdapterPattern.LegacyUser(null));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for insufficient data array")
        void testInsufficientDataArray() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new AdapterPattern.LegacyUser(new String[]{"John", "Doe"}));
        }

        @Test
        @DisplayName("Should handle legacy user with extra data fields gracefully")
        void testExtraDataFields() {
            var adapter = new AdapterPattern.UserDataAdapter();
            var legacy = new AdapterPattern.LegacyUser(new String[]{"John", "Doe", "john@example.com", "extra"});

            AdapterPattern.ModernUser modern = adapter.adapt(legacy);

            assertThat(modern.fullName()).isEqualTo("John Doe");
            assertThat(modern.email()).isEqualTo("john@example.com");
        }
    }

    @Nested
    @DisplayName("Polymorphism - Using adapters through the target interface")
    class PolymorphismTest {

        @Test
        @DisplayName("Should use different adapters polymorphically through MediaPlayer")
        void testPolymorphicUsage() {
            List<AdapterPattern.MediaPlayer> players = List.of(
                    new AdapterPattern.AudioPlayerAdapter(new AdapterPattern.LegacyAudioPlayer()),
                    new AdapterPattern.VideoPlayerAdapter(new AdapterPattern.ThirdPartyVideoPlayer()),
                    new AdapterPattern.AudioClassAdapter()
            );

            assertThat(players).hasSize(3);
            for (AdapterPattern.MediaPlayer player : players) {
                assertThat(player.play("test.file")).isNotBlank();
                assertThat(player.getPlayerType()).isNotBlank();
            }
        }
    }
}
