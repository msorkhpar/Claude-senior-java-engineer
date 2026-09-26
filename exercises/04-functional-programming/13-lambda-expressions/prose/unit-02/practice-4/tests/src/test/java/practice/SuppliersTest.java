package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class SuppliersTest {

    @Test
    void returnsValueOrFallback() {
        assertThat(Suppliers.valueOr("a", () -> "b")).isEqualTo("a");
        assertThat(Suppliers.valueOr(null, () -> "b")).isEqualTo("b");
        assertThat(Suppliers.valueOr("", () -> "b")).isEmpty();
    }

    @Test
    void freshListsAreEmptyAndMutable() {
        List<String> list = Suppliers.freshLists().get();
        assertThat(list).isEmpty();
        list.add("x");
        assertThat(list).containsExactly("x");
    }

    @Test
    void fallbackRunsOnlyWhenNeeded() {
        AtomicInteger calls = new AtomicInteger();
        Supplier<String> costly = () -> {
            calls.incrementAndGet();
            return "computed";
        };
        assertThat(Suppliers.valueOr("present", costly)).isEqualTo("present");
        assertThat(calls.get()).isZero();
        assertThat(Suppliers.valueOr(null, costly)).isEqualTo("computed");
        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    void eachGetReturnsANewList() {
        Supplier<List<String>> factory = Suppliers.freshLists();
        List<String> first = factory.get();
        first.add("x");
        List<String> second = factory.get();
        assertThat(second).isEmpty();
        assertThat(second).isNotSameAs(first);
    }
}
