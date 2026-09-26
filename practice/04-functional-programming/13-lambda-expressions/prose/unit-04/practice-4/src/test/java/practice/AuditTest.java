package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class AuditTest {

    private static final List<Entry> ENTRIES = List.of(
            new Entry("c", true), new Entry("b", false), new Entry("a", true), new Entry("d", false),
            new Entry("c", true));

    @Test
    void listsTheValidIds() {
        assertThat(Audit.audit(ENTRIES).validIds()).containsExactlyInAnyOrder("c", "a", "c");
        assertThat(Audit.audit(List.of()).validIds()).isEmpty();
    }

    @Test
    void validCountCountsOnlyValidEntries() {
        assertThat(Audit.audit(ENTRIES).validCount()).isEqualTo(3);
    }

    @Test
    void seenCountsEveryEntry() {
        assertThat(Audit.audit(ENTRIES).seen()).isEqualTo(5);
    }

    @Test
    void validIdsKeepTheirOrderOnALargeInput() throws Exception {
        List<Entry> many = IntStream.range(0, 200_000)
                .mapToObj(i -> new Entry("id" + i, i % 3 != 0))
                .toList();
        List<String> expected = IntStream.range(0, 200_000)
                .filter(i -> i % 3 != 0)
                .mapToObj(i -> "id" + i)
                .toList();

        ForkJoinPool pool = new ForkJoinPool(8);
        try {
            Report report = pool.submit(() -> Audit.audit(many)).get();
            assertThat(report.validIds()).isEqualTo(expected);
        } finally {
            pool.shutdown();
        }
    }
}
