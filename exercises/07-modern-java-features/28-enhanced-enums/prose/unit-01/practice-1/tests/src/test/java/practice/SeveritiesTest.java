package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static practice.Severities.ExtendedSeverity.CATASTROPHIC;
import static practice.Severities.ExtendedSeverity.CRITICAL;
import static practice.Severities.ExtendedSeverity.TRACE;
import static practice.Severities.StandardSeverity.HIGH;
import static practice.Severities.StandardSeverity.LOW;
import static practice.Severities.StandardSeverity.MEDIUM;

class SeveritiesTest {

    @Test
    void combinesBothEnums() {
        assertThat(Severities.all()).containsExactlyInAnyOrder(TRACE, LOW, MEDIUM, HIGH, CRITICAL, CATASTROPHIC);
        assertThat(Severities.highest(List.of(LOW, CRITICAL, MEDIUM))).contains(CRITICAL);
        assertThat(Severities.highest(List.of(HIGH, TRACE))).contains(HIGH);
    }

    @Test
    void allIsOrderedByLevelAcrossEnums() {
        assertThat(Severities.all()).containsExactly(TRACE, LOW, MEDIUM, HIGH, CRITICAL, CATASTROPHIC);
    }

    @Test
    void allIsReadOnly() {
        List<Severities.Severity> all = Severities.all();
        assertThatThrownBy(() -> all.add(LOW)).isInstanceOf(UnsupportedOperationException.class);
        assertThat(Severities.all()).hasSize(6);
        try {
            Severities.all().set(0, CATASTROPHIC);
        } catch (UnsupportedOperationException refused) {
            // a read-only list is one way to keep all() safe
        }
        assertThat(Severities.all()).first().isEqualTo(TRACE);
    }

    @Test
    void highestOfNothingIsEmpty() {
        assertThat(Severities.highest(List.of())).isEmpty();
    }

    @Test
    void highestComparesLevelsAcrossEnums() {
        assertThat(Severities.highest(List.of(HIGH, CRITICAL))).contains(CRITICAL);
        assertThat(Severities.highest(List.of(CRITICAL, HIGH))).contains(CRITICAL);
    }
}
