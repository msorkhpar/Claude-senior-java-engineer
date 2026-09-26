package practice;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ZipperTest {

    /** A key built at run time, a distinct object from any literal with the same text. */
    private static String key(String text) {
        return new StringBuilder(text).toString();
    }

    /** The result's entries, compared by value whatever kind of map the answer built. */
    private static Map<String, Integer> entries(Map<String, Integer> zipped) {
        return new HashMap<>(zipped);
    }

    @Test
    void zipsPairsIntoAMap() {
        assertThat(entries(Zipper.zip(List.of(key("a"), key("b"), key("c")), List.of(1000, 2000, 3000), (x, y) -> x + y)))
                .isEqualTo(Map.of("a", 1000, "b", 2000, "c", 3000));
        assertThat(Zipper.zip(List.of(), List.of(), (x, y) -> x + y)).isEmpty();
    }

    @Test
    void extraItemsAreIgnored() {
        assertThat(entries(Zipper.zip(List.of(key("a"), key("b"), key("c")), List.of(1000, 2000), (x, y) -> x + y)))
                .isEqualTo(Map.of("a", 1000, "b", 2000));
        assertThat(entries(Zipper.zip(List.of(key("a")), List.of(1000, 2000, 3000), (x, y) -> x + y)))
                .isEqualTo(Map.of("a", 1000));
    }

    @Test
    void duplicateKeysAreMerged() {
        String a = key("a");
        assertThat(entries(Zipper.zip(List.of(a, key("b"), a), List.of(1000, 2000, 5000), (Integer x, Integer y) -> x + y)))
                .isEqualTo(Map.of("a", 6000, "b", 2000));
        String b = key("b");
        assertThat(entries(Zipper.zip(List.of(b, b), List.of(1000, 5000), (existing, incoming) -> existing * 10 + incoming)))
                .isEqualTo(Map.of("b", 15000));
    }

    @Test
    void equalKeysAreOneKey() {
        Map<String, Integer> zipped = Zipper.zip(
                List.of(key("apple"), key("pear"), key("apple")), List.of(1000, 2000, 5000), (x, y) -> x + y);
        assertThat(zipped).hasSize(2);
        assertThat(zipped.get(key("apple"))).isEqualTo(6000);
        assertThat(zipped.get(key("pear"))).isEqualTo(2000);
    }

    @Test
    void mergeTakesExistingFirst() {
        String k = key("k");
        assertThat(entries(Zipper.zip(List.of(k, k), List.of(1000, 5000), (existing, incoming) -> existing - incoming)))
                .isEqualTo(Map.of("k", -4000));
        assertThat(entries(Zipper.zip(List.of(k, k, k), List.of(1000, 2000, 3000), (existing, incoming) -> existing)))
                .isEqualTo(Map.of("k", 1000));
    }
}
