package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class SettingsTest {

    @Test
    void storesAndReadsASetting() {
        Settings s = new Settings();
        s.set("host", "example");
        assertThat(s.isSet("host")).isTrue();
        assertThat(s.get("host")).contains("example");
        assertThat(s.isSet("port")).isFalse();
        assertThat(s.get("port")).isEmpty();
    }

    @Test
    void aSettingMayBePresentWithNoValue() {
        Settings s = new Settings();
        s.set("proxy", null);
        assertThat(s.isSet("proxy")).isTrue();
        assertThat(s.get("proxy")).isEmpty();
    }

    @Test
    void unsetForgetsTheKey() {
        Settings s = new Settings();
        s.set("host", "example");
        s.unset("host");
        assertThat(s.isSet("host")).isFalse();
        assertThat(s.get("host")).isEmpty();
    }

    @Test
    void aValueIsKeptExactlyAsGiven() {
        Settings s = new Settings();
        s.set("k", new String(""));
        s.set("v", " v ");
        assertThat(s.get("k")).contains("");
        assertThat(s.get("v")).contains(" v ");
    }

    @Test
    void aNullKeyIsRefused() {
        Settings s = new Settings();
        assertThatThrownBy(() -> s.set(null, "x")).isInstanceOf(NullPointerException.class);
    }

    @Test
    void theSettingsLiveInAConcurrentHashMap() throws Exception {
        Settings settings = new Settings();
        settings.set("host", "example");
        boolean found = false;
        for (java.lang.reflect.Field field : Settings.class.getDeclaredFields()) {
            if (java.util.Map.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                assertThat(field.get(settings)).isInstanceOf(java.util.concurrent.ConcurrentHashMap.class);
                found = true;
            }
        }
        assertThat(found).as("Settings keeps its settings in a map field").isTrue();
    }
}
