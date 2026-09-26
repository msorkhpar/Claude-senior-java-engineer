package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class ThermostatTest {

    @Test
    void listenersHearEachChange() {
        Thermostat thermostat = new Thermostat();
        List<Integer> first = new ArrayList<>();
        List<Integer> second = new ArrayList<>();
        thermostat.addListener(first::add);
        thermostat.addListener(second::add);
        thermostat.set(20);
        thermostat.set(22);
        assertThat(first).containsExactly(20, 22);
        assertThat(second).containsExactly(20, 22);
        assertThat(thermostat.get()).isEqualTo(22);
    }

    @Test
    void listenersRunWithoutTheLock() {
        Thermostat thermostat = new Thermostat();
        AtomicBoolean answered = new AtomicBoolean();
        AtomicInteger seen = new AtomicInteger();
        thermostat.addListener(t -> {
            Thread reader = new Thread(() -> {
                seen.set(thermostat.get());
                answered.set(true);
            });
            reader.setDaemon(true);
            reader.start();
            try {
                reader.join(5_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        thermostat.set(21);
        assertThat(answered.get()).as("another thread could read while the listener ran").isTrue();
        assertThat(seen.get()).isEqualTo(21);
    }

    @Test
    void listenersSeeTheNewValue() {
        Thermostat thermostat = new Thermostat();
        List<Integer> seen = new ArrayList<>();
        thermostat.addListener(t -> seen.add(thermostat.get()));
        thermostat.set(25);
        thermostat.set(18);
        assertThat(seen).containsExactly(25, 18);
    }

    @Test
    void aListenerMayAddAnother() {
        Thermostat thermostat = new Thermostat();
        List<Integer> late = new ArrayList<>();
        AtomicBoolean added = new AtomicBoolean();
        thermostat.addListener(t -> {
            if (added.compareAndSet(false, true)) {
                thermostat.addListener(late::add);
            }
        });
        thermostat.set(19);
        thermostat.set(23);
        assertThat(late).containsExactly(23);
    }
}
