package practice;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InspectionTest {

    private static final Instant MORNING = Instant.parse("2027-02-14T09:30:00Z");

    private static Inspection filed() {
        return new Inspection("dock-7", Date.from(MORNING),
                new String("loose railing"), new String("blocked exit"), new String("no hard hats"));
    }

    @Test
    void reportsWhenAndWhatTheInspectorFound() {
        Inspection i = filed();

        assertThat(i.getSite()).isEqualTo("dock-7");
        assertThat(i.getInspectedAt().toInstant()).isEqualTo(MORNING);
        assertThat(i.getDefects()).containsExactly("loose railing", "blocked exit", "no hard hats");
        assertThat(i.defectCount()).isEqualTo(3);
    }

    @Test
    void aReportThatChangesTheDateItWasGivenChangesNothing() {
        Inspection i = filed();

        i.getInspectedAt().setTime(0L);

        assertThat(i.getInspectedAt().toInstant()).isEqualTo(MORNING);
    }

    @Test
    void anExportThatEditsTheDefectListChangesNothing() {
        Inspection i = filed();

        List<String> defects = i.getDefects();
        try {
            defects.set(1, "none");
        } catch (UnsupportedOperationException refused) {
            // a list the caller can't change is fine too
        }

        assertThat(i.getDefects()).containsExactly("loose railing", "blocked exit", "no hard hats");
    }
}
