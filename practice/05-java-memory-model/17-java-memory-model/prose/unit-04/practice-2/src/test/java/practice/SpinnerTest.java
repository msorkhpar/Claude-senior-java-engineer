package practice;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class SpinnerTest {

    @Test
    void runsStepsUntilAStopIsRequested() {
        Spinner spinner = new Spinner();
        assertThat(spinner.stopRequested()).isFalse();
        AtomicInteger runs = new AtomicInteger();
        int steps = spinner.runUntilStopped(() -> {
            if (runs.incrementAndGet() == 3) {
                spinner.requestStop();
            }
        });
        assertThat(steps).isEqualTo(3);
        assertThat(runs.get()).isEqualTo(3);
        assertThat(spinner.stopRequested()).isTrue();
    }

    @Test
    void aStopRequestedFirstRunsNoStep() {
        Spinner spinner = new Spinner();
        spinner.requestStop();
        AtomicInteger runs = new AtomicInteger();
        assertThat(spinner.runUntilStopped(runs::incrementAndGet)).isZero();
        assertThat(runs.get()).isZero();
    }

    @Test
    void theStopFlagIsVolatile() {
        Spinner spinner = new Spinner();
        spinner.requestStop();
        assertThat(spinner.stopRequested()).isTrue();
        Field flag = null;
        for (Field f : Spinner.class.getDeclaredFields()) {
            if (f.getType() == boolean.class && !Modifier.isStatic(f.getModifiers())) {
                flag = f;
            }
        }
        assertThat(flag).as("a boolean field holds the stop request").isNotNull();
        assertThat(Modifier.isVolatile(flag.getModifiers())).as("the stop flag is volatile").isTrue();
    }
}
