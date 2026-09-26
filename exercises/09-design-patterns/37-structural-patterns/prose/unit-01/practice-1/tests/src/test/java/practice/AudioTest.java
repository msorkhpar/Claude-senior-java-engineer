package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class AudioTest {

    @Test
    void bothAdaptersPlayThroughTheLegacyPlayer() {
        Audio.MediaPlayer object = new Audio.AudioPlayerAdapter(new Audio.LegacyAudioPlayer());
        Audio.MediaPlayer klass = new Audio.AudioClassAdapter();

        assertThat(object.play("song.mp3")).isEqualTo("Playing MP3: song.mp3");
        assertThat(klass.play("song.mp3")).isEqualTo("Playing MP3: song.mp3");
        assertThat(object.getPlayerType()).isEqualTo("Audio (Object Adapter)");
        assertThat(klass.getPlayerType()).isEqualTo("Audio (Class Adapter)");
    }

    @Test
    void theObjectAdapterUsesThePlayerItWasGiven() {
        Audio.LegacyAudioPlayer loud = new Audio.LegacyAudioPlayer() {
            @Override
            public String playMp3(String filename) {
                return "LOUD " + super.playMp3(filename);
            }
        };

        Audio.MediaPlayer adapter = new Audio.AudioPlayerAdapter(loud);

        assertThat(adapter.play("song.mp3")).isEqualTo("LOUD Playing MP3: song.mp3");
    }

    @Test
    void theClassAdapterCallsTheMethodItInherits() {
        Audio.MediaPlayer adapter = new Audio.AudioClassAdapter() {
            @Override
            public String playMp3(String filename) {
                return "Quiet " + super.playMp3(filename);
            }
        };

        assertThat(adapter.play("song.mp3")).isEqualTo("Quiet Playing MP3: song.mp3");
    }

    @Test
    void theCallPassesThroughUnchanged() {
        Audio.MediaPlayer object = new Audio.AudioPlayerAdapter(new Audio.LegacyAudioPlayer());
        Audio.MediaPlayer klass = new Audio.AudioClassAdapter();

        assertThat(object.play(" song.mp3")).isEqualTo("Playing MP3:  song.mp3");
        assertThat(klass.play(" song.mp3")).isEqualTo("Playing MP3:  song.mp3");
        assertThatIllegalArgumentException().isThrownBy(() -> object.play("  "));
        assertThatIllegalArgumentException().isThrownBy(() -> klass.play("  "));
    }

    @Test
    void aNullPlayerIsRefusedAtOnce() {
        assertThatNullPointerException().isThrownBy(() -> new Audio.AudioPlayerAdapter(null));
    }
}
