package practice;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JitFlagsTest {

    @Test
    void readsTheJitFlagsAndSkipsTheRest() {
        JitFlags defaults = JitFlags.parse(List.of("-Xmx4g", "-XX:+UseG1GC", "-cp", "app.jar"));
        assertThat(defaults.tiered()).isTrue();
        assertThat(defaults.topTier()).isEqualTo(4);
        assertThat(defaults.maxInlineSize()).isEqualTo(35);
        assertThat(defaults.freqInlineSize()).isEqualTo(325);
        assertThat(defaults.printCompilation()).isFalse();
        assertThat(defaults.printInlining()).isFalse();

        JitFlags tuned = JitFlags.parse(List.of("-XX:TieredStopAtLevel=1", "-XX:MaxInlineSize=50",
                "-XX:FreqInlineSize=400", "-XX:+PrintCompilation", "-Xss1m"));
        assertThat(tuned.tiered()).isTrue();
        assertThat(tuned.topTier()).isEqualTo(1);
        assertThat(tuned.maxInlineSize()).isEqualTo(50);
        assertThat(tuned.freqInlineSize()).isEqualTo(400);
        assertThat(tuned.printCompilation()).isTrue();
    }

    @Test
    void aMinusTurnsAFlagOff() {
        JitFlags c2Only = JitFlags.parse(List.of("-XX:-TieredCompilation"));

        assertThat(c2Only.tiered()).isFalse();
        assertThat(c2Only.topTier()).isEqualTo(4);
        assertThat(JitFlags.parse(List.of("-XX:TieredStopAtLevel=1", "-XX:-TieredCompilation")).topTier()).isEqualTo(4);
    }

    @Test
    void theLastOccurrenceWins() {
        JitFlags flags = JitFlags.parse(List.of("-XX:MaxInlineSize=50", "-XX:+PrintCompilation",
                "-XX:MaxInlineSize=20", "-XX:-PrintCompilation"));

        assertThat(flags.maxInlineSize()).isEqualTo(20);
        assertThat(flags.printCompilation()).isFalse();
    }

    @Test
    void theStopLevelIsZeroToFour() {
        assertThat(JitFlags.parse(List.of("-XX:TieredStopAtLevel=0")).topTier()).isZero();
        assertThat(JitFlags.parse(List.of("-XX:TieredStopAtLevel=4")).topTier()).isEqualTo(4);
        assertThatThrownBy(() -> JitFlags.parse(List.of("-XX:TieredStopAtLevel=5")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> JitFlags.parse(List.of("-XX:TieredStopAtLevel=-1")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void printInliningNeedsTheDiagnosticUnlock() {
        assertThatThrownBy(() -> JitFlags.parse(List.of("-XX:+PrintInlining")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> JitFlags.parse(List.of("-XX:-UnlockDiagnosticVMOptions", "-XX:+PrintInlining")))
                .isInstanceOf(IllegalArgumentException.class);

        JitFlags unlocked = JitFlags.parse(List.of("-XX:+UnlockDiagnosticVMOptions", "-XX:+PrintInlining"));
        assertThat(unlocked.printInlining()).isTrue();
    }
}
