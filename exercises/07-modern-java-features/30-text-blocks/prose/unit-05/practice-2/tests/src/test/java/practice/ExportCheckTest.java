package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExportCheckTest {

    private static final String EXPECTED = """
            sku,warehouse,on_hand
            KB-104,ROT,18
            KB-221,ROT,0
            MS-009,GDN,240
            """;

    @Test
    void theSameExportMatches() {
        String actual = new StringBuilder(EXPECTED).toString();

        assertThat(ExportCheck.firstDifference(EXPECTED, actual)).isZero();
        assertThat(ExportCheck.verdict(EXPECTED, actual)).isEqualTo("export matches");
    }

    @Test
    void aChangedValueIsReportedOnItsLine() {
        String actual = EXPECTED.replace("KB-221,ROT,0", "KB-221,ROT,6");

        assertThat(ExportCheck.firstDifference(EXPECTED, actual)).isEqualTo(3);
        assertThat(ExportCheck.verdict(EXPECTED, actual))
                .isEqualTo("line 3 differs: expected [KB-221,ROT,0], got [KB-221,ROT,6]");
    }

    @Test
    void aMissingRowIsReported() {
        String actual = "sku,warehouse,on_hand\nKB-104,ROT,18\nKB-221,ROT,0\n";

        assertThat(ExportCheck.firstDifference(EXPECTED, actual)).isEqualTo(4);
    }

    @Test
    void anExportWrittenOnWindowsMatches() {
        String actual = "sku,warehouse,on_hand\r\nKB-104,ROT,18\r\nKB-221,ROT,0\r\nMS-009,GDN,240\r\n";

        assertThat(ExportCheck.firstDifference(EXPECTED, actual)).isZero();
    }

    @Test
    void anExportWithoutANewlineAfterItsLastRowMatches() {
        String actual = "sku,warehouse,on_hand\nKB-104,ROT,18\nKB-221,ROT,0\nMS-009,GDN,240";

        assertThat(ExportCheck.firstDifference(EXPECTED, actual)).isZero();
    }

    @Test
    void aTrailingSpaceIsADifference() {
        String actual = EXPECTED.replace("KB-104,ROT,18\n", "KB-104,ROT,18 \n");

        assertThat(ExportCheck.firstDifference(EXPECTED, actual)).isEqualTo(2);
        String lastRow = EXPECTED.replace("MS-009,GDN,240\n", "MS-009,GDN,240 \n");
        assertThat(ExportCheck.firstDifference(EXPECTED, lastRow)).isEqualTo(4);
    }
}
