package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Thermostat {

    private final List<Integer> history = new ArrayList<>();
    private int target = 20;

    public Thermostat() {
    }

    /** Accepts 5..30 inclusive; null throws NPE("celsius"), out of range IAE("Target out of range: " + celsius). */
    public void setTarget(Integer celsius) {
        Objects.requireNonNull(celsius);
        if (celsius < 5 || celsius > 30) {
            throw new IllegalArgumentException("Target out of range: " + celsius);
        }
        history.add(celsius);
        target = celsius;
    }

    public int getTarget() {
        return target;
    }

    /** The accepted targets, oldest first. */
    public List<Integer> history() {
        return List.copyOf(history);
    }
}
