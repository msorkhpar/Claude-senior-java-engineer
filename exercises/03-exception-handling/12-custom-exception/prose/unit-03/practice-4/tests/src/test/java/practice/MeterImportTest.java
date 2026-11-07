package practice;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

class MeterImportTest {

    private static final List<String> GOOD = List.of("1204", " 98", "7731");
    private static final List<String> BAD = List.of("1204", "98", "77,31", "15");

    private final List<LogRecord> records = new ArrayList<>();
    private final Logger root = Logger.getLogger("");
    private final Handler capture = new Handler() {
        @Override
        public void publish(LogRecord record) {
            synchronized (records) {
                records.add(record);
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
        }
    };

    @BeforeEach
    void captureTheLog() {
        capture.setLevel(Level.ALL);
        root.addHandler(capture);
    }

    @AfterEach
    void stopCapturing() {
        root.removeHandler(capture);
    }

    private List<LogRecord> warningsAndAbove() {
        synchronized (records) {
            return records.stream().filter(r -> r.getLevel().intValue() >= Level.WARNING.intValue()).toList();
        }
    }

    @Test
    void aGoodFileIsImportedAndLogsNoProblem() {
        String report = new MeterImport().runNightly("meters-0412.csv", GOOD);

        assertThat(report).isEqualTo("imported meters-0412.csv: 9033 kWh");
        assertThat(warningsAndAbove()).isEmpty();
    }

    @Test
    void aBadFileIsSkippedAndTheReasonIsLogged() {
        String report = new MeterImport().runNightly("meters-0413.csv", BAD);

        assertThat(report).isEqualTo("skipped meters-0413.csv");
        assertThat(warningsAndAbove()).isNotEmpty();
    }

    @Test
    void oneSkippedFileIsOneProblemInTheLog() {
        new MeterImport().runNightly("meters-0414.csv", BAD);

        assertThat(warningsAndAbove()).hasSize(1);
    }

    @Test
    void theLoggedProblemCarriesItsStackTrace() {
        new MeterImport().runNightly("meters-0415.csv", BAD);

        assertThat(warningsAndAbove()).allSatisfy(r -> assertThat(r.getThrown())
                .as("the record's exception, which the log prints as a stack trace")
                .isInstanceOf(MeterImport.ImportException.class)
                .hasMessageContaining("meters-0415.csv")
                .hasMessageContaining("3"));
    }

    @Test
    void theLoggedProblemLeadsToTheLineThatCouldNotBeParsed() {
        new MeterImport().runNightly("meters-0416.csv", BAD);

        assertThat(warningsAndAbove()).allSatisfy(r -> assertThat(r.getThrown()).isNotNull()
                .hasRootCauseInstanceOf(NumberFormatException.class)
                .hasRootCauseMessage("For input string: \"77,31\""));
    }
}
