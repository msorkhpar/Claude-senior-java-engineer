package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FactoryTest {

    @Test
    void buildsNObjects() {
        assertThat(Factory.createN(3, () -> "x")).containsExactly("x", "x", "x");
        assertThat(Factory.createN(1, () -> 7)).containsExactly(7);
    }

    @Test
    void eachElementIsAFreshObject() {
        List<List<String>> lists = Factory.createN(3, ArrayList::new);
        assertThat(lists).hasSize(3);
        lists.get(0).add("only in the first");

        assertThat(lists.get(0)).isNotSameAs(lists.get(1));
        assertThat(lists.get(1)).isNotSameAs(lists.get(2));
        assertThat(lists.get(1)).isEmpty();
        assertThat(lists.get(2)).isEmpty();
    }

    @Test
    void zeroAsksTheFactoryForNothing() {
        int[] calls = {0};
        Supplier<String> counting = () -> {
            calls[0]++;
            return "x";
        };

        assertThat(Factory.createN(0, counting)).isEmpty();
        assertThat(calls[0]).isZero();
    }

    @Test
    void aNegativeCountIsRejected() {
        assertThatThrownBy(() -> Factory.createN(-1, () -> "x"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("n must not be negative: -1");
    }
}
