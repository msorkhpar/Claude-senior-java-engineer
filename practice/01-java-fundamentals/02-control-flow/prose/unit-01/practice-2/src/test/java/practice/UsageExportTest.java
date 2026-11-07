package practice;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class UsageExportTest {

    private static UsageExport export() {
        Map<String, Integer> quotas = new HashMap<>();
        quotas.put("initech", 2_500);
        quotas.put("hooli", 40_000);
        return new UsageExport(Set.of("piedpiper", "vandelay"), quotas);
    }

    @Test
    void freeTenantsGetTheFreeQuotaAndContractsTheirOwn() {
        UsageExport export = export();

        assertThat(export.quota("vandelay")).isEqualTo(100);
        assertThat(export.quota("initech")).isEqualTo(2_500);
        assertThat(export.quota("hooli")).isEqualTo(40_000);
    }

    @Test
    void ratiosAreWrittenAsFractions() {
        UsageExport export = export();

        Object value = export.value(UsageExport.Metric.ratio("cache-hit-rate", 0.875));

        assertThat(value).isInstanceOf(Double.class).isEqualTo(0.875);
        assertThat(export.line("piedpiper", List.of(UsageExport.Metric.ratio("cache-hit-rate", 0.875))))
                .isEqualTo("piedpiper;quota=100;cache-hit-rate=0.875");
    }

    @Test
    void aContractWithoutAQuotaIsExportedAsUnlimited() {
        UsageExport export = export();

        assertThatCode(() -> export.quota("umbrella")).doesNotThrowAnyException();
        assertThat(export.quota("umbrella")).isNull();
        assertThat(export.line("umbrella", List.of())).isEqualTo("umbrella;quota=unlimited");
    }

    @Test
    void countsAreWrittenAsWholeNumbers() {
        UsageExport export = export();

        Object value = export.value(UsageExport.Metric.count("jobs", 1_234));

        assertThat(value).isInstanceOf(Integer.class).isEqualTo(1_234);
        assertThat(export.line("initech", List.of(UsageExport.Metric.count("jobs", 1_234))))
                .isEqualTo("initech;quota=2500;jobs=1234");
    }
}
