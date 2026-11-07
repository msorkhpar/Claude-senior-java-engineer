package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** The inventory below is made up for the tests; it is not the history of real HotSpot options. */
class StartOptionsCheckTest {

    private static final Map<String, StartOptionsCheck.History> INVENTORY = Map.of(
            "UseWideCardTable", new StartOptionsCheck.History(12, 16, 19),
            "TraceRemsetScan", new StartOptionsCheck.History(14, 20, 23),
            "PromotionBatchSize", new StartOptionsCheck.History(19, null, null),
            "UseSpinParking", new StartOptionsCheck.History(null, 22, 24));

    private static final List<String> SCRIPT = List.of(
            "-Xmx2g", "-XX:+UseG1GC", "-XX:+UseWideCardTable", "-XX:+TraceRemsetScan",
            "-XX:PromotionBatchSize=64", "-XX:-UseSpinParking");

    @Test
    void anOptionNobodyRemovedIsSupported() {
        StartOptionsCheck.Result result = StartOptionsCheck.check(List.of("-XX:+UseG1GC", "-XX:MaxGCPauseMillis=150"),
                INVENTORY, 22);

        assertThat(result.starts()).isTrue();
        assertThat(result.deprecated()).isEmpty();
        assertThat(result.obsolete()).isEmpty();
        assertThat(result.expired()).isEmpty();
    }

    @Test
    void anOldReleaseStartsWithEveryOption() {
        StartOptionsCheck.Result result = StartOptionsCheck.check(SCRIPT, INVENTORY, 11);

        assertThat(result.starts()).isTrue();
        assertThat(result.deprecated()).isEmpty();
        assertThat(result.expired()).isEmpty();
    }

    @Test
    void aDeprecatedOptionStillStarts() {
        StartOptionsCheck.Result result = StartOptionsCheck.check(List.of("-XX:+TraceRemsetScan", "-Xss512k"),
                INVENTORY, 17);

        assertThat(result.starts()).isTrue();
        assertThat(result.deprecated()).containsExactly("-XX:+TraceRemsetScan");
    }

    @Test
    void anObsoleteOptionIsReportedButTheJvmStillStarts() {
        StartOptionsCheck.Result result = StartOptionsCheck.check(List.of("-XX:+TraceRemsetScan", "-XX:+UseG1GC"),
                INVENTORY, 21);

        assertThat(result.obsolete()).containsExactly("-XX:+TraceRemsetScan");
        assertThat(result.starts()).isTrue();
    }

    @Test
    void anExpiredOptionStopsTheJvm() {
        StartOptionsCheck.Result result = StartOptionsCheck.check(SCRIPT, INVENTORY, 21);

        assertThat(result.expired()).containsExactly("-XX:+UseWideCardTable");
        assertThat(result.starts()).isFalse();
    }

    @Test
    void eachStageStartsInTheReleaseTheInventoryNames() {
        StartOptionsCheck.Result result = StartOptionsCheck.check(SCRIPT, INVENTORY, 19);

        assertThat(result.deprecated()).containsExactly("-XX:+TraceRemsetScan", "-XX:PromotionBatchSize=64");
        assertThat(result.expired()).containsExactly("-XX:+UseWideCardTable");
        assertThat(StartOptionsCheck.check(SCRIPT, INVENTORY, 22).obsolete())
                .containsExactly("-XX:+TraceRemsetScan", "-XX:-UseSpinParking");
    }
}
