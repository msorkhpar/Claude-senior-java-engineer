package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SourcesTest {

    @Test
    void countsFromStartByStep() {
        IntSupplier up = Sources.counter(0, 1);
        assertThat(up.getAsInt()).isZero();
        assertThat(up.getAsInt()).isEqualTo(1);
        assertThat(up.getAsInt()).isEqualTo(2);

        IntSupplier down = Sources.counter(10, -5);
        assertThat(down.getAsInt()).isEqualTo(10);
        assertThat(down.getAsInt()).isEqualTo(5);
        assertThat(down.getAsInt()).isZero();
    }

    @Test
    void cyclesThroughTheItems() {
        Supplier<String> colours = Sources.cycling(List.of("red", "green", "blue"));
        List<String> seen = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            seen.add(colours.get());
        }
        assertThat(seen).containsExactly("red", "green", "blue", "red", "green", "blue", "red");

        Supplier<String> single = Sources.cycling(List.of("only"));
        assertThat(single.get()).isEqualTo("only");
        assertThat(single.get()).isEqualTo("only");
    }

    @Test
    void eachSourceKeepsItsOwnPosition() {
        IntSupplier a = Sources.counter(0, 1);
        IntSupplier b = Sources.counter(100, 1);
        assertThat(a.getAsInt()).isZero();
        assertThat(b.getAsInt()).isEqualTo(100);
        assertThat(a.getAsInt()).isEqualTo(1);
        assertThat(b.getAsInt()).isEqualTo(101);

        Supplier<String> x = Sources.cycling(List.of("x1", "x2", "x3"));
        Supplier<String> y = Sources.cycling(List.of("y1", "y2", "y3"));
        assertThat(x.get()).isEqualTo("x1");
        assertThat(y.get()).isEqualTo("y1");
        assertThat(x.get()).isEqualTo("x2");
    }

    @Test
    void anEmptyCycleIsRejectedUpFront() {
        assertThatThrownBy(() -> Sources.cycling(List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("items must not be empty");
    }

    @Test
    void laterChangesToTheListDoNotLeakIn() {
        List<String> items = new ArrayList<>(List.of("a", "b"));
        Supplier<String> cycle = Sources.cycling(items);
        items.set(1, "CHANGED");
        items.add("extra");

        assertThat(cycle.get()).isEqualTo("a");
        assertThat(cycle.get()).isEqualTo("b");
        assertThat(cycle.get()).isEqualTo("a");
    }
}
