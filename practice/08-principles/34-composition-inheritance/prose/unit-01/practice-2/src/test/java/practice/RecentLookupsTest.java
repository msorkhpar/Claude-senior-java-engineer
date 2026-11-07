package practice;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RecentLookupsTest {

    @Test
    void answersLikeAMap() {
        RecentLookups<String, String> geo = new RecentLookups<>(5);
        assertThat(geo.nextToDrop()).isNull();
        geo.put("192.0.2.10", "NL");
        geo.put("198.51.100.7", "BR");
        geo.put("192.0.2.10", "DE");
        assertThat(geo.get("192.0.2.10")).isEqualTo("DE");
        assertThat(geo.containsKey("198.51.100.7")).isTrue();
        assertThat(geo).hasSize(2);
    }

    @Test
    void neverHoldsMoreThanItsCapacity() {
        RecentLookups<String, String> geo = new RecentLookups<>(3);
        for (int i = 1; i <= 7; i++) {
            geo.put("203.0.113." + i, "C" + i);
            assertThat(geo.size()).isLessThanOrEqualTo(3);
        }
        assertThat(geo).containsKey("203.0.113.7");
        assertThat(geo).doesNotContainKey("203.0.113.1");
        assertThat(geo.nextToDrop()).isNotEqualTo("203.0.113.7");
    }

    @Test
    void aBulkWarmUpKeepsTheBound() {
        Map<String, String> warmUp = new LinkedHashMap<>();
        for (int i = 1; i <= 6; i++) warmUp.put("198.51.100." + i, "W" + i);
        RecentLookups<String, String> geo = new RecentLookups<>(4);
        geo.putAll(warmUp);
        assertThat(geo).hasSize(4);
        assertThat(geo).containsOnlyKeys("198.51.100.3", "198.51.100.4", "198.51.100.5", "198.51.100.6");
    }

    @Test
    void anAnswerReadRecentlyOutlivesOneNobodyRead() {
        RecentLookups<String, String> geo = new RecentLookups<>(3);
        geo.put("a.example", "FR");
        geo.put("b.example", "JP");
        geo.put("c.example", "CA");
        assertThat(geo.get("a.example")).isEqualTo("FR");
        assertThat(geo.nextToDrop()).isEqualTo("b.example");
        geo.put("d.example", "KE");
        assertThat(geo).containsOnlyKeys("a.example", "c.example", "d.example");
    }

    @Test
    void holdsExactlyItsCapacity() {
        RecentLookups<String, String> geo = new RecentLookups<>(4);
        for (int i = 1; i <= 4; i++) geo.put("192.0.2." + (40 + i), "Z" + i);
        assertThat(geo).hasSize(4);
        assertThat(geo.nextToDrop()).isEqualTo("192.0.2.41");
    }
}
