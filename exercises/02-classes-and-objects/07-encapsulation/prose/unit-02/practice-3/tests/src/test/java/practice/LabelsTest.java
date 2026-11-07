package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LabelsTest {

    @Test
    void aLabelGivesItsNameTrimmedAndInLowerCase() {
        assertThat(new Labels.Label("  Needs-Review ").name()).isEqualTo("needs-review");
        assertThat(new Labels.Label("flaky").name()).isEqualTo("flaky");
    }

    @Test
    void twoSpellingsOfOneLabelAreOneEntry() {
        Set<Labels.Label> labels = Labels.distinct(List.of("Regression", " regression", "ui", "REGRESSION "));

        assertThat(labels).hasSize(2);
        assertThat(new Labels.Label("Ui ")).isEqualTo(new Labels.Label("ui"));
    }

    @Test
    void theLogShowsTheLabelAsItIsUsed() {
        assertThat(new Labels.Label(" Perf").toString()).isEqualTo("Label[name=perf]");
    }
}
