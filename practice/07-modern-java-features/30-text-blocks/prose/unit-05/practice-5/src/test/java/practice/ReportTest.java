package practice;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class ReportTest {

    private static String lines(String... lines) {
        return String.join("\n", lines);
    }

    @Test
    void fillsTheReport() {
        assertThat(Report.summary(new String("Q2 2024"), 999, null)).isEqualTo(lines("Report: Q2 2024", "Revenue: $999"));
        assertThat(Report.summary(new String("Q4 2023"), 1_000_000, null))
                .isEqualTo(lines("Report: Q4 2023", "Revenue: $1,000,000"));
    }

    @Test
    void aGrowthLineWritesItsPercentSign() {
        assertThat(Report.summary(new String("Q1 2024"), 1_000_000, 15))
                .isEqualTo(lines("Report: Q1 2024", "Revenue: $1,000,000", "Growth: 15%"));
        assertThat(Report.summary(new String("Q3 2024"), 1_234_567, -3))
                .isEqualTo(lines("Report: Q3 2024", "Revenue: $1,234,567", "Growth: -3%"));
    }

    @Test
    void groupingDoesNotFollowTheDefaultLocale() {
        Locale before = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            assertThat(Report.summary(new String("Q1 2024"), 1_000_000, null))
                    .isEqualTo(lines("Report: Q1 2024", "Revenue: $1,000,000"));
            Locale.setDefault(Locale.FRANCE);
            assertThat(Report.summary(new String("Q2 2024"), 2_500_000, null))
                    .isEqualTo(lines("Report: Q2 2024", "Revenue: $2,500,000"));
            Locale.setDefault(Locale.forLanguageTag("th-TH-u-nu-thai"));
            assertThat(Report.summary(new String("Q1 2024"), 1_000_000, 15))
                    .isEqualTo(lines("Report: Q1 2024", "Revenue: $1,000,000", "Growth: 15%"));
        } finally {
            Locale.setDefault(before);
        }
    }
}
