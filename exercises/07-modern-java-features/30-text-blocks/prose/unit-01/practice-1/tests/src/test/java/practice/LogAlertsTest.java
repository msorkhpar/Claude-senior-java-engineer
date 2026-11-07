package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LogAlertsTest {

    @Test
    void aQuietLogRaisesNoAlert() {
        List<String> log = List.of("08:00:01 INFO  checkout started", "08:00:07 WARN  disk at 91%");

        assertThat(LogAlerts.errorLines(log)).isEmpty();
    }

    @Test
    void theShareNamesTheHostAndItsLogs() {
        String share = LogAlerts.shareFor("db-02");

        assertThat(share).contains("db-02").contains("logs");
    }

    @Test
    void aLineWithTheWordErrorRaisesTheAlert() {
        List<String> log = List.of(
                "11:42:00 INFO  order 5512 placed",
                "11:42:03 ERROR payment gateway timed out",
                "11:42:04 INFO  retry scheduled",
                "11:43:10 ERROR: stock service unreachable");

        assertThat(LogAlerts.errorLines(log)).containsExactly(
                "11:42:03 ERROR payment gateway timed out",
                "11:43:10 ERROR: stock service unreachable");
    }

    @Test
    void errorInsideALongerWordRaisesNothing() {
        List<String> log = List.of(
                "11:50:00 INFO  batch done, ERRORS: 0",
                "11:50:01 DEBUG dns answer NOERRORCODE",
                "11:50:02 INFO  TERROR-9 shipped");

        assertThat(LogAlerts.errorLines(log)).isEmpty();
    }

    @Test
    void theShareStartsWithTwoBackslashes() {
        String share = LogAlerts.shareFor("db-02");

        assertThat(share).startsWith("\\\\db-02\\");
        assertThat(share.charAt(0)).isEqualTo('\\');
        assertThat(share.charAt(1)).isEqualTo('\\');
        assertThat(share.charAt(2)).isEqualTo('d');
    }

    @Test
    void everyFolderNameOfTheShareArrivesIntact() {
        String share = LogAlerts.shareFor("mq-11");

        assertThat(share.substring(share.indexOf("mq-11")).split("\\\\", -1))
                .containsExactly("mq-11", "logs", "nightly");
        assertThat(share.chars().filter(Character::isISOControl).count()).isZero();
    }
}
