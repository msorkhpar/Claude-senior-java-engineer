package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReportCacheTest {

    /** Serves "text of <name>" and records every fetch; names in `missing` fail with FileNotFoundException. */
    static final class FakeSource implements ReportCache.ReportSource {
        final List<String> fetched = new ArrayList<>();
        final List<String> missing = new ArrayList<>();
        RuntimeException bug;                         // thrown by every fetch when set

        @Override
        public String fetch(String name) throws IOException {
            fetched.add(name);
            if (bug != null) throw bug;
            if (missing.contains(name)) throw new FileNotFoundException("no report " + name);
            return "text of " + name;
        }
    }

    @Test
    void fetchesEachReportOnceAndThenServesItFromMemory() throws IOException {
        FakeSource source = new FakeSource();
        ReportCache cache = new ReportCache(source);
        assertThat(cache.get("weekly-churn")).isEqualTo("text of weekly-churn");
        assertThat(cache.get("ledger-2026-10")).isEqualTo("text of ledger-2026-10");
        assertThat(cache.get("weekly-churn")).isEqualTo("text of weekly-churn");
        assertThat(source.fetched).containsExactly("weekly-churn", "ledger-2026-10");
    }

    @Test
    void aFailingSourceFailsTheCallWithAnIOException() throws IOException {
        FakeSource source = new FakeSource();
        source.missing.add("audit-q2");
        ReportCache cache = new ReportCache(source);
        assertThatThrownBy(() -> cache.get("audit-q2")).isInstanceOf(IOException.class);
        assertThat(cache.get("weekly-churn")).isEqualTo("text of weekly-churn");
    }

    @Test
    void theCallerReceivesTheExceptionTheSourceThrew() {
        FileNotFoundException thrown = new FileNotFoundException("share offline");
        ReportCache cache = new ReportCache(name -> { throw thrown; });
        Throwable caught = catchThrowable(() -> cache.get("audit-q2"));
        assertThat(caught).isSameAs(thrown);
    }

    @Test
    void anUncheckedFailureOfTheSourceReachesTheCallerUnchanged() {
        FakeSource source = new FakeSource();
        source.bug = new IllegalArgumentException("report names may not contain '/'");
        ReportCache cache = new ReportCache(source);
        Throwable caught = catchThrowable(() -> cache.get("q3/summary"));
        assertThat(caught).isSameAs(source.bug);
    }
}
