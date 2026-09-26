package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class NullableMapTest {

    /** A fresh String object, never the interned literal. */
    private static String text(String s) {
        return new String(s.toCharArray());
    }

    @Test
    void storesAndReadsValues() {
        NullableMap map = new NullableMap();
        map.put(text("host"), text("db1"));
        map.put(text("user"), text("app"));
        assertThat(map.get(text("host"))).isEqualTo("db1");
        assertThat(map.get(text("user"))).isEqualTo("app");
        assertThat(map.containsKey(text("host"))).isTrue();
        assertThat(map.get(text("port"))).isNull();
        assertThat(map.containsKey(text("port"))).isFalse();
    }

    @Test
    void keepsANullValue() {
        NullableMap map = new NullableMap();
        map.put(text("proxy"), text("p1"));
        map.put(text("proxy"), null);
        assertThat(map.containsKey(text("proxy"))).isTrue();
        assertThat(map.get(text("proxy"))).isNull();
    }

    @Test
    void emptyStringIsARealValue() {
        NullableMap map = new NullableMap();
        map.put(text("suffix"), "");
        map.put(text("prefix"), text(""));
        map.put(text("word"), text("null"));
        map.put(text("proxy"), null);
        assertThat(map.get(text("suffix"))).isNotNull().isEmpty();
        assertThat(map.get(text("word"))).isEqualTo("null");
        assertThat(map.get(text("prefix"))).isNotNull().isEmpty();
        assertThat(map.get(text("proxy"))).isNull();
    }

    @Test
    void refusesANullKey() {
        NullableMap map = new NullableMap();
        assertThatThrownBy(() -> map.put(null, text("x"))).isInstanceOf(NullPointerException.class);
    }

    @Test
    void noStoredStringDoublesAsTheSentinel() {
        NullableMap map = new NullableMap();
        map.put("nul", "\u0000");
        map.put("zero", new String("0"));
        assertThat(map.get("nul")).isEqualTo("\u0000");
        assertThat(map.get("zero")).isEqualTo("0");
        map.put("gone", null);
        assertThat(map.get("gone")).isNull();
        assertThat(map.containsKey("gone")).isTrue();
    }
}
