package practice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TracingTest {

    /** The target: a map-backed store that refuses unknown keys. */
    static final class MapStore implements Tracing.Store {
        private final Map<String, String> data = new LinkedHashMap<>();

        @Override
        public String get(String key) {
            String value = data.get(key);
            if (value == null) {
                throw new NoSuchElementException("no key " + key);
            }
            return value;
        }

        @Override
        public String put(String key, String value) {
            return data.put(key, value);
        }

        @Override
        public int size() {
            return data.size();
        }

        @Override
        public String toString() {
            return "MapStore" + data;
        }
    }

    @Test
    void logsEachCallAndDelegates() {
        MapStore target = new MapStore();
        List<String> log = new ArrayList<>();
        Tracing.Store store = Tracing.logging(target, Tracing.Store.class, log);

        store.put("a", "1");
        store.put("b", "2");
        assertThat(store.get("a")).isEqualTo("1");

        assertThat(target.get("b")).isEqualTo("2");
        assertThat(log).containsExactly("put(a, 1)", "put(b, 2)", "get(a)");
    }

    @Test
    void aCallWithoutArgumentsLogsEmptyParentheses() {
        MapStore target = new MapStore();
        target.put("a", "1");
        List<String> log = new ArrayList<>();
        Tracing.Store store = Tracing.logging(target, Tracing.Store.class, log);

        assertThat(store.size()).isEqualTo(1);
        assertThat(log).containsExactly("size()");
    }

    @Test
    void theTargetsExceptionReachesTheCaller() {
        List<String> log = new ArrayList<>();
        Tracing.Store store = Tracing.logging(new MapStore(), Tracing.Store.class, log);

        assertThatThrownBy(() -> store.get("zzz"))
                .isExactlyInstanceOf(NoSuchElementException.class)
                .hasMessage("no key zzz");
        assertThat(log).containsExactly("get(zzz)");
    }

    @Test
    void objectMethodsGoToTheTargetUnlogged() {
        MapStore target = new MapStore();
        target.put("a", "1");
        List<String> log = new ArrayList<>();
        Tracing.Store store = Tracing.logging(target, Tracing.Store.class, log);

        assertThat(store.toString()).isEqualTo("MapStore{a=1}");
        assertThat(store.hashCode()).isEqualTo(target.hashCode());
        assertThat(store.equals(target)).isTrue();
        assertThat(log).isEmpty();
    }
}
