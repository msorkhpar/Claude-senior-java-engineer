package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductRegistryTest {

    @Test
    void createsProductsByName() {
        ProductRegistry registry = new ProductRegistry();
        registry.register("list", ArrayList::new);
        registry.register("map", TreeMap::new);

        assertThat(registry.create("list")).isInstanceOf(ArrayList.class);
        assertThat(registry.create("map")).isInstanceOf(TreeMap.class);
    }

    @Test
    void eachCreateMakesANewProduct() {
        ProductRegistry registry = new ProductRegistry();
        registry.register("list", ArrayList::new);

        Object first = registry.create("list");
        Object second = registry.create("list");

        assertThat(second).isNotSameAs(first);
    }

    @Test
    void anUnknownNameIsRefused() {
        ProductRegistry registry = new ProductRegistry();
        registry.register("list", ArrayList::new);

        assertThatThrownBy(() -> registry.create("set"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("set");
    }

    @Test
    void aTypedCreateChecksTheType() {
        ProductRegistry registry = new ProductRegistry();
        int[] runs = {0};
        registry.register("list", () -> {
            runs[0]++;
            return new ArrayList<>();
        });

        List<?> list = registry.create("list", List.class);

        assertThat(list).isEmpty();
        assertThat(runs[0]).isEqualTo(1);
        assertThatThrownBy(() -> registry.create("list", Map.class)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aNameIsRegisteredOnce() {
        ProductRegistry registry = new ProductRegistry();
        registry.register("coll", ArrayList::new);

        assertThatThrownBy(() -> registry.register("coll", TreeMap::new)).isInstanceOf(IllegalStateException.class);
        assertThat(registry.create("coll")).isInstanceOf(ArrayList.class);
    }
}
