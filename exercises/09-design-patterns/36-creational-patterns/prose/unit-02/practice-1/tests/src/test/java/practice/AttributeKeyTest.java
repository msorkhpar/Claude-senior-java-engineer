package practice;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AttributeKeyTest {

    /** A name built at run time, as one read from a span would be: never the literal itself. */
    private static String fresh(String name) {
        return new String(name.toCharArray());
    }

    @Test
    void everyKeyCarriesItsName() {
        assertThat(AttributeKey.of(fresh("http.route")).name()).isEqualTo("http.route");
        assertThat(AttributeKey.of(fresh("db.system")).name()).isEqualTo("db.system");
        assertThat(AttributeKey.of(fresh("checkout.cart_size")).name()).isEqualTo("checkout.cart_size");
        assertThat(AttributeKey.of(fresh("tenant-81"))).isEqualTo(AttributeKey.of(fresh("tenant-81")));

        Map<AttributeKey, Integer> attributes = new HashMap<>();
        attributes.put(AttributeKey.of(fresh("net.peer.port")), 5432);
        attributes.put(AttributeKey.of(fresh("feature.flag")), 1);
        assertThat(attributes.get(AttributeKey.of(fresh("net.peer.port")))).isEqualTo(5432);
        assertThat(attributes.get(AttributeKey.of(fresh("feature.flag")))).isEqualTo(1);
    }

    @Test
    void aStandardNameGivesTheSameKeyEveryTime() {
        for (String name : AttributeKey.STANDARD) {
            AttributeKey first = AttributeKey.of(fresh(name));
            AttributeKey second = AttributeKey.of(fresh(name));
            assertThat(second).as(name).isSameAs(first);
        }
    }

    @Test
    void namesOutsideTheStandardOnesAreNotKept() {
        for (int i = 0; i < 10_000; i++) {
            assertThat(AttributeKey.of("order-" + i).name()).isEqualTo("order-" + i);
        }
        assertThat(AttributeKey.kept()).isLessThanOrEqualTo(AttributeKey.STANDARD.size());
    }
}
