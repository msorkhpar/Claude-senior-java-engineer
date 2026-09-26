package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NullSafeTest {

    @Test
    void collectsNicknamesAndCountsItems() {
        assertThat(NullSafe.nicknames(List.of(
                new NullSafe.Person("Robert", "Bob"),
                new NullSafe.Person("Katherine", "Kate"))))
                .containsExactly("Bob", "Kate");
        assertThat(NullSafe.nicknames(List.of())).isEmpty();
        assertThat(NullSafe.nicknames(List.of(
                new NullSafe.Person("Robert", "Bob"),
                new NullSafe.Person("Bobby", "Bob"),
                new NullSafe.Person("Zed", ""))))
                .containsExactly("Bob", "Bob", "");
        assertThat(NullSafe.countItems(new String[] {"a", "b", "c"})).isEqualTo(3);
        assertThat(NullSafe.countItems(new String[0])).isZero();
        assertThat(NullSafe.countItems(new String[] {"a", null, "c"})).isEqualTo(3);
    }

    @Test
    void missingNicknamesAreSkipped() {
        assertThat(NullSafe.nicknames(List.of(
                new NullSafe.Person("Robert", "Bob"),
                new NullSafe.Person("Ann", null),
                new NullSafe.Person("William", "Will"))))
                .containsExactly("Bob", "Will");
    }

    @Test
    void aNullArrayCountsZero() {
        assertThat(NullSafe.countItems(null)).isZero();
    }
}
