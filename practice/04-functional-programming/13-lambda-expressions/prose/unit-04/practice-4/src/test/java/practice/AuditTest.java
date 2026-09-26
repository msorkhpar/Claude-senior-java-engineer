package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AuditTest {

    private static final List<Entry> ENTRIES = List.of(
            new Entry("a", true), new Entry("b", false), new Entry("c", true), new Entry("d", false));

    @Test
    void listsTheValidIds() {
        assertThat(Audit.audit(ENTRIES).validIds()).containsExactly("a", "c");
        assertThat(Audit.audit(List.of()).validIds()).isEmpty();
    }

    @Test
    void validCountCountsOnlyValidEntries() {
        assertThat(Audit.audit(ENTRIES).validCount()).isEqualTo(2);
    }

    @Test
    void seenCountsEveryEntry() {
        assertThat(Audit.audit(ENTRIES).seen()).isEqualTo(4);
    }
}
