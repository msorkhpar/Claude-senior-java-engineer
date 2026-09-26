package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShoutTest {

    @Test
    void shoutsTheValueAndEachWord() {
        assertThat(Shout.of(new Holder("hello")).get()).isEqualTo("HELLO");
        assertThat(Shout.each().apply("goodbye")).isEqualTo("GOODBYE");
        assertThat(Shout.each().apply("hello")).isEqualTo("HELLO");
        assertThat(Shout.all(List.of("a", "b"))).containsExactly("A", "B");
    }

    @Test
    void theReceiverIsFixedWhenTheSupplierIsMade() {
        Holder holder = new Holder("hello");
        Supplier<String> shout = Shout.of(holder);
        holder.set("bye");
        assertThat(shout.get()).isEqualTo("HELLO");
    }

    @Test
    void aMissingValueFailsWhenTheSupplierIsMade() {
        assertThatThrownBy(() -> Shout.of(new Holder(null)))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("value must not be null");
    }
}
