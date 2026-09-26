package practice;

import java.util.List;

public class Thermostat {

    public Thermostat() {
    }

    /** Accepts 5..30 inclusive; null throws NPE("celsius"), out of range IAE("Target out of range: " + celsius). */
    public void setTarget(Integer celsius) {
        throw new UnsupportedOperationException("write setTarget");
    }

    public int getTarget() {
        throw new UnsupportedOperationException("write getTarget");
    }

    /** The accepted targets, oldest first. */
    public List<Integer> history() {
        throw new UnsupportedOperationException("write history");
    }
}
