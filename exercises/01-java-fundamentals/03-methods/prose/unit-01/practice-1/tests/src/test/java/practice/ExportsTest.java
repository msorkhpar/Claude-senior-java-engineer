package practice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ExportsTest {

    @Test
    void theServicesOwnFormatGetsItsExtension() {
        assertThat(new Exports.Exporter().fileName("ledger-2026-11")).isEqualTo("ledger-2026-11.dat");
        assertThat(Exports.forFormat("xlsx").fileName("audit")).isEqualTo("audit.dat");
    }

    @Test
    void anExporterPickedAtRunTimeNamesItsOwnFormat() {
        Exports.Exporter csv = Exports.forFormat("csv");
        Exports.Exporter json = Exports.forFormat("json");
        assertThat(csv.fileName("ledger-2026-11")).isEqualTo("ledger-2026-11.csv");
        assertThat(json.fileName("audit")).isEqualTo("audit.json");
    }

    @Test
    void aFormatBuiltOnAnotherFormatKeepsItsOwnExtension() {
        Exports.Exporter gzip = Exports.forFormat("csv.gz");
        assertThat(gzip.fileName("ledger-2026-11")).isEqualTo("ledger-2026-11.csv.gz");
        Exports.CsvExporter typedAsCsv = new Exports.GzipCsvExporter();
        assertThat(typedAsCsv.fileName("audit")).isEqualTo("audit.csv.gz");
    }
}
