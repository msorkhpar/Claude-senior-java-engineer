package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CleanupTest {

    @Test
    void runsWorkThenCleanup() {
        List<String> log = new ArrayList<>();
        Cleanup.run(() -> log.add("work"), () -> log.add("cleanup"));
        assertThat(log).containsExactly("work", "cleanup");
    }

    @Test
    void aWorkFailureStillRunsCleanup() {
        List<String> log = new ArrayList<>();
        IllegalArgumentException workFailure = new IllegalArgumentException("work failed");
        assertThatThrownBy(() -> Cleanup.run(() -> {
            throw workFailure;
        }, () -> log.add("cleanup"))).isSameAs(workFailure);
        assertThat(log).containsExactly("cleanup");

        Error fatal = new Error("fatal");
        assertThatThrownBy(() -> Cleanup.run(() -> {
            throw fatal;
        }, () -> log.add("cleanup after error"))).isSameAs(fatal);
        assertThat(log).containsExactly("cleanup", "cleanup after error");
    }

    @Test
    void aCleanupFailureKeepsTheWorkFailureAsItsCause() {
        IllegalArgumentException workFailure = new IllegalArgumentException("work failed");
        IllegalStateException cleanupFailure = new IllegalStateException("cleanup failed");
        assertThatThrownBy(() -> Cleanup.run(() -> {
            throw workFailure;
        }, () -> {
            throw cleanupFailure;
        })).isSameAs(cleanupFailure);
        assertThat(cleanupFailure.getCause()).isSameAs(workFailure);

        IllegalStateException otherWorkFailure = new IllegalStateException("work broke");
        IllegalArgumentException otherCleanupFailure = new IllegalArgumentException("cleanup broke");
        assertThatThrownBy(() -> Cleanup.run(() -> {
            throw otherWorkFailure;
        }, () -> {
            throw otherCleanupFailure;
        })).isSameAs(otherCleanupFailure);
        assertThat(otherCleanupFailure.getCause()).isSameAs(otherWorkFailure);
    }

    @Test
    void aCleanupFailureAloneIsThrownWithNoCause() {
        List<String> log = new ArrayList<>();
        IllegalStateException cleanupFailure = new IllegalStateException("cleanup failed");
        assertThatThrownBy(() -> Cleanup.run(() -> log.add("work"), () -> {
            throw cleanupFailure;
        })).isSameAs(cleanupFailure);
        assertThat(cleanupFailure.getCause()).isNull();
        assertThat(log).containsExactly("work");
    }

    @Test
    void theWorkFailureIsTheCauseNotASuppressedOne() {
        IllegalArgumentException workFailure = new IllegalArgumentException("work failed");
        IllegalStateException cleanupFailure = new IllegalStateException("cleanup failed");
        assertThatThrownBy(() -> Cleanup.run(() -> {
            throw workFailure;
        }, () -> {
            throw cleanupFailure;
        })).isSameAs(cleanupFailure).hasCause(workFailure);
        assertThat(cleanupFailure.getSuppressed()).isEmpty();
    }
}
