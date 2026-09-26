package practice;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.*;

class StoresTest {

    @Test
    void bothStoresReadAndList() {
        var rw = new Stores.ReadWriteStore();
        rw.write("b", "2");
        rw.write("a", "1");
        assertThat(rw.read("a")).isEqualTo("1");
        assertThat(rw.listKeys()).containsExactly("a", "b");
        rw.delete("a");
        assertThat(rw.exists("a")).isFalse();
        assertThat(rw.size()).isEqualTo(1);
        var ro = new Stores.ReadOnlyStore(Map.of("y", "20", "x", "10"));
        assertThat(ro.read("x")).isEqualTo("10");
        assertThat(ro.exists("z")).isFalse();
        assertThat(ro.read("z")).isNull();
        assertThat(ro.listKeys()).containsExactly("x", "y");
        assertThat(ro.isEmpty()).isFalse();
        var hashed = new Stores.ReadWriteStore();
        hashed.write("b", "1");
        hashed.write("q", "2");
        hashed.write("m", "3");
        assertThat(hashed.listKeys()).containsExactly("b", "m", "q");
        Map<String, String> inserted = new LinkedHashMap<>();
        inserted.put("q", "1");
        inserted.put("b", "2");
        inserted.put("m", "3");
        assertThat(new Stores.ReadOnlyStore(inserted).listKeys()).containsExactly("b", "m", "q");
    }

    @Test
    void aReadOnlyStoreOffersNoWriteMethods() {
        assertThat(new Stores.ReadOnlyStore(Map.of("k", "v")).read("k")).isEqualTo("v");
        assertThat(Stores.Writable.class.isAssignableFrom(Stores.ReadOnlyStore.class)).isFalse();
        assertThat(Stores.Readable.class.isAssignableFrom(Stores.ReadOnlyStore.class)).isTrue();
        assertThat(Stores.Writable.class.isAssignableFrom(Stores.ReadWriteStore.class)).isTrue();
    }

    @Test
    void aReadOnlyStoreKeepsItsOwnCopy() {
        Map<String, String> source = new HashMap<>();
        source.put("x", String.valueOf(10));
        var ro = new Stores.ReadOnlyStore(source);
        source.put("x", String.valueOf(99));
        source.put("late", String.valueOf(1));
        assertThat(ro.read("x")).isEqualTo("10");
        assertThat(ro.exists("late")).isFalse();
        assertThat(ro.listKeys()).containsExactly("x");
        for (Map<String, String> other : List.<Map<String, String>>of(new TreeMap<>(), new ConcurrentHashMap<>())) {
            other.put("x", String.valueOf(10));
            var copy = new Stores.ReadOnlyStore(other);
            other.put("x", String.valueOf(99));
            other.put("late", String.valueOf(1));
            assertThat(copy.read("x")).isEqualTo("10");
            assertThat(copy.listKeys()).containsExactly("x");
        }
    }

    @Test
    void aNullMapIsAnEmptyStore() {
        var ro = new Stores.ReadOnlyStore(null);
        assertThat(ro.isEmpty()).isTrue();
        assertThat(ro.size()).isZero();
        assertThat(ro.listKeys()).isNotNull().isEmpty();
        assertThat(ro.read("x")).isNull();
    }
}
