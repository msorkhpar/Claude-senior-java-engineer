package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class HandlersTest {

    @Test
    void eachFactoryDoesItsJob() {
        List<String> sink = new ArrayList<>();
        Handlers.appendTo(sink).accept("A");
        Handlers.appendTo(sink).accept("B");
        assertThat(sink).containsExactly("A", "B");

        assertThat(Handlers.freshList().get()).isEmpty();
        assertThat(Handlers.length().apply("hello")).isEqualTo(5);
        assertThat(Handlers.startsWithA().test("Alice")).isTrue();
        assertThat(Handlers.startsWithA().test("Bob")).isFalse();
        assertThat(Handlers.larger().apply(3000, 7000)).isEqualTo(7000);
        assertThat(Handlers.larger().apply(9000, 2000)).isEqualTo(9000);

        List<String> seen = new ArrayList<>();
        Handlers.both(s -> seen.add("print " + s), s -> seen.add("log " + s)).accept("x");
        assertThat(seen).containsExactlyInAnyOrder("print x", "log x");
    }

    @Test
    void eachGetMakesANewList() {
        Supplier<List<String>> factory = Handlers.freshList();
        List<String> first = factory.get();
        first.add("used");
        List<String> second = factory.get();

        assertThat(second).isEmpty();
        assertThat(second).isNotSameAs(first);
    }

    @Test
    void bothRunsTheFirstConsumerFirst() {
        List<String> seen = new ArrayList<>();
        Handlers.both(s -> seen.add("print " + s), s -> seen.add("log " + s)).accept("x");

        assertThat(seen).containsExactly("print x", "log x");
    }
}
