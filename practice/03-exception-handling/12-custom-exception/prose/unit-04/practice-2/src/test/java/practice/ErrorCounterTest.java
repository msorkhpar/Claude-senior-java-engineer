package practice;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ErrorCounterTest {

    private static final ErrorCounter.Rule SEVERE = line -> line.contains(" SEVERE ");

    /** A log holding these bytes; decoding errors are reported, as Files.newBufferedReader does. */
    private static ErrorCounter.Log bytes(byte[] content) {
        return charset -> new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(content), charset.newDecoder()));
    }

    private static ErrorCounter.Log utf8(String text) {
        return bytes(text.getBytes(StandardCharsets.UTF_8));
    }

    /** A log on a flaky mount: serves the text, then the next read fails with the given exception. */
    private static ErrorCounter.Log failsAfter(String text, IOException failure) {
        return charset -> new BufferedReader(new Reader() {
            private final Reader served = new StringReader(text);

            @Override
            public int read(char[] buf, int off, int len) throws IOException {
                int n = served.read(buf, off, len);
                if (n == -1) throw failure;
                return n;
            }

            @Override
            public void close() {
            }
        });
    }

    private static Map<String, ErrorCounter.Log> logs(Object... nameThenLog) {
        Map<String, ErrorCounter.Log> logs = new LinkedHashMap<>();
        for (int i = 0; i < nameThenLog.length; i += 2) {
            logs.put((String) nameThenLog[i], (ErrorCounter.Log) nameThenLog[i + 1]);
        }
        return logs;
    }

    @Test
    void countsTheErrorLinesOfEachLog() {
        ErrorCounter.Report report = new ErrorCounter(SEVERE).scan(logs(
                "billing-03.log", utf8("""
                        04:10:02 INFO  invoice 7731 sent to Zürich
                        04:10:09 SEVERE ledger lock timeout
                        04:11:40 SEVERE ledger lock timeout
                        """),
                "billing-04.log", utf8("04:12:00 INFO  idle\n")));

        assertThat(report.errors()).containsExactly(
                Map.entry("billing-03.log", 2L), Map.entry("billing-04.log", 0L));
        assertThat(report.skipped()).isEmpty();
    }

    @Test
    void aLogThatCannotBeOpenedIsSkipped() {
        NoSuchFileException gone = new NoSuchFileException("/path/to/logs/billing-05.log");
        ErrorCounter.Report report = new ErrorCounter(SEVERE).scan(logs(
                "billing-05.log", (ErrorCounter.Log) charset -> { throw gone; },
                "billing-06.log", utf8("05:00:00 SEVERE disk full\n")));

        assertThat(report.skipped()).containsOnlyKeys("billing-05.log");
        assertThat(report.skipped().get("billing-05.log")).isSameAs(gone);
        assertThat(report.errors()).containsExactly(Map.entry("billing-06.log", 1L));
    }

    @Test
    void aLogFromAnOldWarehouseHostIsCounted() {
        byte[] latin1 = """
                06:30:00 SEVERE scanner offline in Mönchengladbach
                06:31:12 INFO  pallet 4410 booked by Ågren
                06:32:45 SEVERE scanner offline in Mönchengladbach
                """.getBytes(StandardCharsets.ISO_8859_1);
        ErrorCounter.Report report = new ErrorCounter(SEVERE).scan(logs("warehouse-2.log", bytes(latin1)));

        assertThat(report.skipped()).isEmpty();
        assertThat(report.errors()).containsExactly(Map.entry("warehouse-2.log", 2L));
    }

    @Test
    void aLogWhoseReadFailsHalfWayIsSkippedAndTheBatchGoesOn() {
        IOException stale = new IOException("Stale file handle");
        ErrorCounter.Report report = new ErrorCounter(SEVERE).scan(logs(
                "billing-07.log", failsAfter("07:00:00 SEVERE retry queue full\n07:00:01 INFO  ", stale),
                "billing-08.log", utf8("07:05:00 SEVERE retry queue full\n")));

        assertThat(report.skipped()).containsOnlyKeys("billing-07.log");
        assertThat(report.skipped().get("billing-07.log")).isSameAs(stale);
        assertThat(report.errors()).containsExactly(Map.entry("billing-08.log", 1L));
    }

    @Test
    void aRuleThatThrowsStopsTheBatch() {
        ErrorCounter.Rule picky = line -> {
            if (line.isBlank()) throw new IllegalArgumentException("rule can't classify a blank line");
            return line.contains(" SEVERE ");
        };
        ErrorCounter counter = new ErrorCounter(picky);
        Map<String, ErrorCounter.Log> logs = logs(
                "billing-09.log", utf8("08:00:00 INFO  start\n\n08:00:02 SEVERE late\n"),
                "billing-10.log", utf8("08:10:00 SEVERE late\n"));

        assertThatThrownBy(() -> counter.scan(logs))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("rule can't classify a blank line");
    }
}
