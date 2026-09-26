package practice;

import org.junit.jupiter.api.Test;

import practice.Settings.AppName;
import practice.Settings.DebugMode;
import practice.Settings.MaxRetries;
import practice.Settings.TimeoutMs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SettingsTest {

    @Test
    void parsesEachSettingToItsOwnType() {
        Integer retries = Settings.parse(new MaxRetries(), "5");
        String name = Settings.parse(new AppName(), " Shop ");
        Boolean debug = Settings.parse(new DebugMode(), "true");
        Long timeout = Settings.parse(new TimeoutMs(), "2500");
        assertThat(retries).isEqualTo(5);
        assertThat(name).isEqualTo("Shop");
        assertThat(debug).isTrue();
        assertThat(timeout).isEqualTo(2500L);
        assertThat(Settings.values()).containsExactly(new MaxRetries(), new AppName(), new DebugMode(), new TimeoutMs());
        assertThat(Settings.byKey("app.name")).contains(new AppName());
        assertThat(Settings.byKey("no.such.key")).isEmpty();
        Integer spaced = Settings.parse(new MaxRetries(), " 5 ");
        Long spacedTimeout = Settings.parse(new TimeoutMs(), " 2500 ");
        assertThat(spaced).isEqualTo(5);
        assertThat(spacedTimeout).isEqualTo(2500L);
    }

    @Test
    void timeoutTakesALong() {
        Long timeout = Settings.parse(new TimeoutMs(), "10000000000");
        assertThat(timeout).isEqualTo(10_000_000_000L);
    }

    @Test
    void debugAcceptsOnlyTrueOrFalse() {
        assertThatThrownBy(() -> Settings.parse(new DebugMode(), "yes")).isInstanceOf(IllegalArgumentException.class);
        Boolean off = Settings.parse(new DebugMode(), " false ");
        assertThat(off).isFalse();
        assertThatThrownBy(() -> Settings.parse(new DebugMode(), "TRUE")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Settings.parse(new DebugMode(), "")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Settings.parse(new DebugMode(), "   ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void missingTextGivesTheDefault() {
        String name = Settings.parse(new AppName(), null);
        Long timeout = Settings.parse(new TimeoutMs(), null);
        assertThat(name).isEqualTo("DefaultApp");
        assertThat(timeout).isEqualTo(5000L);
    }

    @Test
    void findsAKeyBuiltAtRuntime() {
        String key = String.join(".", "timeout", "ms");
        assertThat(Settings.byKey(key)).contains(new TimeoutMs());
    }
}
