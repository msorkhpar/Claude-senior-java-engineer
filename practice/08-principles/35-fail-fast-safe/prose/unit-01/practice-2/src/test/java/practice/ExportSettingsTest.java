package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class ExportSettingsTest {

    private static Properties file(String text) {
        Properties p = new Properties();
        try {
            p.load(new StringReader(text));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return p;
    }

    @Test
    void aFileThatSetsEverythingIsReadAsWritten() {
        ExportSettings s = ExportSettings.from(file("""
                export.dir=/srv/reports/out
                export.batch.size=250
                export.retries=7
                """));

        assertThat(s.dir).isEqualTo("/srv/reports/out");
        assertThat(s.batchSize).isEqualTo(250);
        assertThat(s.retries).isEqualTo(7);
    }

    @Test
    void settingsTheFileLeavesOutTakeTheirDefaults() {
        ExportSettings s = ExportSettings.from(file("""
                export.dir=/srv/reports/archive
                """));

        assertThat(s.dir).isEqualTo("/srv/reports/archive");
        assertThat(s.batchSize).isEqualTo(500);
        assertThat(s.retries).isEqualTo(2);
    }

    @Test
    void aMisspeltSettingStopsTheStartAndIsNamed() {
        assertThatThrownBy(() -> ExportSettings.from(file("""
                export.dir=/srv/reports/out
                export.retires=5
                """)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("export.retires");
    }

    @Test
    void aMissingExportDirectoryStopsTheStartAndIsNamed() {
        assertThatThrownBy(() -> ExportSettings.from(file("""
                export.batch.size=100
                """)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("export.dir");
    }

    @Test
    void aNumberThatDoesNotParseStopsTheStartAndIsNamed() {
        assertThatThrownBy(() -> ExportSettings.from(file("""
                export.dir=/srv/reports/out
                export.retries=three
                """)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("export.retries");
    }
}
