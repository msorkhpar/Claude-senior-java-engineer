package practice;

import org.junit.jupiter.api.Test;

import practice.EnumIndex.Dial;
import practice.EnumIndex.SharedDial;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnumIndexTest {

    @Test
    void findsAConstantByItsKey() {
        Function<Integer, Optional<Dial>> byCode = EnumIndex.indexBy(Dial.class, Dial::code);
        assertThat(byCode.apply(44)).contains(Dial.UK);
        assertThat(byCode.apply(1)).contains(Dial.US);
        assertThat(byCode.apply(49)).contains(Dial.DE);
    }

    @Test
    void findsKeysAboveTheIntegerCache() {
        Function<Integer, Optional<Dial>> byCode = EnumIndex.indexBy(Dial.class, Dial::code);
        assertThat(byCode.apply(Integer.parseInt("358"))).contains(Dial.FI);
        assertThat(byCode.apply(Integer.parseInt("353"))).contains(Dial.IE);
    }

    @Test
    void unknownKeyIsEmpty() {
        Function<Integer, Optional<Dial>> byCode = EnumIndex.indexBy(Dial.class, Dial::code);
        assertThat(byCode.apply(7)).isEmpty();
    }

    @Test
    void sharedKeyIsRefused() {
        assertThatThrownBy(() -> EnumIndex.indexBy(SharedDial.class, SharedDial::code))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> EnumIndex.indexBy(Dial.class, d -> d.code() > 300 ? Integer.valueOf(Integer.parseInt("358")) : Integer.valueOf(d.code())))
                .as("IE and FI both keyed 358")
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void buildsTheIndexOnce() {
        AtomicInteger reads = new AtomicInteger();
        Function<Integer, Optional<Dial>> byCode = EnumIndex.indexBy(Dial.class, d -> {
            reads.incrementAndGet();
            return d.code();
        });
        assertThat(reads.get()).as("keys read while building").isEqualTo(Dial.values().length);
        assertThat(byCode.apply(49)).contains(Dial.DE);
        assertThat(byCode.apply(1)).contains(Dial.US);
        assertThat(byCode.apply(7)).isEmpty();
        assertThat(reads.get()).as("keys read after three lookups").isEqualTo(Dial.values().length);
    }
}
