package practice;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Residents' parking permits, one per car. Residents type their plate as it is printed
 * ("KL-482-T", "kl 482 t"), and the street cameras report it as they read it.
 */
public class Permits {
    private final Map<String, String> permitByPlate = new HashMap<>();

    /** When two plates, however written, are the same car. */
    static String plateKey(String plate) {
        return plate.replace(" ", "").replace("-", "").toUpperCase(Locale.ROOT);
    }

    /** A resident buys a permit for a car. A car holds at most one permit. */
    public void register(String plate, String permitId) {
        if (permitByPlate.containsKey(plateKey(plate)))
            throw new IllegalArgumentException("plate already holds a permit: " + plate);
        permitByPlate.put(plateKey(plate), permitId);
    }

    /** A resident moves the permit to another car, which must not hold a permit already. */
    public void transfer(String fromPlate, String toPlate) {
        if (permitByPlate.containsKey(toPlate.replace("-", "").toUpperCase(Locale.ROOT)))
            throw new IllegalArgumentException("plate already holds a permit: " + toPlate);
        String permitId = permitByPlate.remove(plateKey(fromPlate));
        if (permitId == null)
            throw new IllegalArgumentException("no permit on " + fromPlate);
        permitByPlate.put(plateKey(toPlate), permitId);
    }

    /** A street camera asks whether the car it sees may park here. */
    public boolean isPermitted(String plateAsRead) {
        return permitByPlate.containsKey(plateKey(plateAsRead));
    }
}
