package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlertingTest {

    @Test
    void aSuccessDoesNotAlert() {
        List<String> ran = new ArrayList<>();
        List<RuntimeException> alerts = new ArrayList<>();
        Alerting.run(() -> ran.add("work"), alerts::add);
        assertThat(ran).containsExactly("work");
        assertThat(alerts).isEmpty();
    }

    @Test
    void aFailureIsAlertedAndRethrown() {
        IllegalArgumentException bad = new IllegalArgumentException("bad");
        List<RuntimeException> alerts = new ArrayList<>();
        assertThatThrownBy(() -> Alerting.run(() -> {
            throw bad;
        }, alerts::add))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("bad");
        assertThat(alerts).containsExactly(bad);
    }

    @Test
    void aFailingAlertDoesNotHideTheFailure() {
        IllegalArgumentException bad = new IllegalArgumentException("bad");
        assertThatThrownBy(() -> Alerting.run(() -> {
            throw bad;
        }, failure -> {
            throw new IllegalStateException("alert down");
        })).isSameAs(bad);
    }

    @Test
    void theAlertFailureIsKeptAsSuppressed() {
        IllegalArgumentException bad = new IllegalArgumentException("bad");
        IllegalStateException alertDown = new IllegalStateException("alert down");
        assertThatThrownBy(() -> Alerting.run(() -> {
            throw bad;
        }, failure -> {
            throw alertDown;
        })).isSameAs(bad);
        assertThat(bad.getSuppressed()).containsExactly(alertDown);
    }

    @Test
    void theRethrownFailureIsTheOriginalObject() {
        IllegalArgumentException bad = new IllegalArgumentException("bad");
        assertThatThrownBy(() -> Alerting.run(() -> {
            throw bad;
        }, failure -> { })).isSameAs(bad);
    }
}
