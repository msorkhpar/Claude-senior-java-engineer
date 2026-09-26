package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LruCacheTest {

    @Test
    void evictsTheOldestWhenFull() {
        LruCache<String, String> cache = new LruCache<>(3);
        cache.put("a", "1");
        cache.put("b", "2");
        cache.put("c", "3");
        cache.put("d", "4");

        assertThat(cache.keys()).containsExactly("b", "c", "d");
        assertThat(cache.get("a")).isNull();
        assertThat(cache.get("d")).isEqualTo("4");
    }

    @Test
    void getMakesAnEntryRecent() {
        LruCache<String, String> cache = new LruCache<>(3);
        cache.put("a", "1");
        cache.put("b", "2");
        cache.put("c", "3");
        assertThat(cache.get("a")).isEqualTo("1");
        cache.put("d", "4");

        assertThat(cache.keys()).containsExactly("c", "a", "d");
        assertThat(cache.get("b")).isNull();
    }

    @Test
    void updatingAKeyEvictsNothing() {
        LruCache<String, Integer> cache = new LruCache<>(2);
        cache.put("a", 1000);
        cache.put("b", 2000);
        cache.put("b", 3000);

        assertThat(cache.keys()).containsExactly("a", "b");
        assertThat(cache.get("a")).isEqualTo(1000);
        assertThat(cache.get("b")).isEqualTo(3000);
    }

    @Test
    void aCapacityBelowOneIsRefused() {
        assertThatThrownBy(() -> new LruCache<String, String>(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new LruCache<String, String>(-3))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
