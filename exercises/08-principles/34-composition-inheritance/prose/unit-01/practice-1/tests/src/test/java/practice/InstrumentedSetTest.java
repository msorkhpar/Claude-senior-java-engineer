package practice;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class InstrumentedSetTest {

    /** A string built at run time, never an interned literal. */
    private static String s(String v) {
        return new String(v);
    }

    @Test
    void countsEveryAttemptedAdd() {
        InstrumentedSet<String> set = new InstrumentedSet<>(new HashSet<>());
        assertThat(set.add(s("a"))).isTrue();
        assertThat(set.add(s("b"))).isTrue();
        assertThat(set.add(s("a"))).isFalse();
        assertThat(set.getAddCount()).isEqualTo(3);
        assertThat(set).hasSize(2).contains("a", "b");
    }

    @Test
    void addAllCountsEachElementOnce() {
        InstrumentedSet<String> set = new InstrumentedSet<>(new HashSet<>());
        set.addAll(List.of(s("snap"), s("crackle"), s("pop")));
        assertThat(set.getAddCount()).isEqualTo(3);
        assertThat(set).containsExactlyInAnyOrder("snap", "crackle", "pop");
        InstrumentedSet<String> again = new InstrumentedSet<>(new HashSet<>());
        again.add(s("a"));
        again.addAll(List.of(s("a"), s("b")));
        assertThat(again.getAddCount()).as("a duplicate offered to addAll is still counted").isEqualTo(3);
    }

    @Test
    void theWrapperIsAViewOfTheSetItWraps() {
        Set<String> backing = new HashSet<>(List.of(s("old")));
        InstrumentedSet<String> set = new InstrumentedSet<>(backing);
        set.add(s("new"));
        assertThat(backing).containsExactlyInAnyOrder("old", "new");
        backing.add(s("later"));
        assertThat(set.contains(s("later"))).isTrue();
        assertThat(set).hasSize(3);
    }

    @Test
    void equalsHashCodeAndToStringFollowTheWrappedSet() {
        Set<String> backing = new HashSet<>(List.of(s("x"), s("y")));
        InstrumentedSet<String> set = new InstrumentedSet<>(backing);
        Set<String> other = new HashSet<>(List.of(s("y"), s("x")));
        assertThat(set.equals(other)).isTrue();
        assertThat(other.equals(set)).isTrue();
        assertThat(set.hashCode()).isEqualTo(other.hashCode());
        assertThat(set.toString()).isEqualTo(backing.toString());
    }

    @Test
    void aNullSetIsRefusedAtOnce() {
        assertThatThrownBy(() -> new InstrumentedSet<String>(null)).isInstanceOf(NullPointerException.class);
    }
}
