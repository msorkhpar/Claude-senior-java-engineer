package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class LookupsTest {

    @Test
    void looksUpPresentValues() {
        assertThat(Lookups.nickname(Optional.of(new Member("Sam", "  ")))).contains("  ");
        assertThat(Lookups.nickname(Optional.of(new Member("Robert", "bob")))).contains("BOB");
        assertThat(Lookups.nickname(Optional.empty())).isEmpty();
        assertThat(Lookups.trimmedLength(Optional.of("  hi "))).contains(2);
        assertThat(Lookups.trimmedLength(Optional.empty())).isEmpty();
        List<String> existing = new ArrayList<>(List.of("x"));
        assertThat(Lookups.orCreate(Optional.of(existing), ArrayList::new)).isSameAs(existing);
        List<String> created = Lookups.orCreate(Optional.<List<String>>empty(), ArrayList::new);
        assertThat(created).isEmpty();
    }

    @Test
    void aMemberWithoutANicknameHasNone() {
        assertThat(Lookups.nickname(Optional.of(new Member("Ann", null)))).isEmpty();
    }

    @Test
    void blankTextHasNoLength() {
        assertThat(Lookups.trimmedLength(Optional.of("\u0001 \u0001"))).isEmpty();
        assertThat(Lookups.trimmedLength(Optional.of("   "))).isEmpty();
        assertThat(Lookups.trimmedLength(Optional.of(""))).isEmpty();
    }

    @Test
    void theFactoryRunsOnlyWhenTheValueIsMissing() {
        AtomicInteger calls = new AtomicInteger();
        Supplier<String> factory = () -> {
            calls.incrementAndGet();
            return "made";
        };
        assertThat(Lookups.orCreate(Optional.of("kept"), factory)).isEqualTo("kept");
        assertThat(calls).hasValue(0);
        assertThat(Lookups.orCreate(Optional.empty(), factory)).isEqualTo("made");
        assertThat(calls).hasValue(1);
    }
}
