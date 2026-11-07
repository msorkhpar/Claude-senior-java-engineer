package practice;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Residents' parking permits, one per car. Residents type their plate as it is printed
 * ("KL-482-T", "kl 482 t"), and the street cameras report it as they read it. Each permit
 * has a code printed on it, which residents read out to support on the phone.
 */
public class Permits {
    private final Map<String, String> codeByPlate = new HashMap<>();   // plateKey -> codeKey
    private final Map<String, String> plateByCode = new HashMap<>();   // codeKey -> plateKey

    /** When two plates, however written, are the same car. */
    static String plateKey(String plate) {
        return plate.replace(" ", "").replace("-", "").toUpperCase(Locale.ROOT);
    }

    /** When two permit codes, as typed or read out, are the same permit. */
    static String codeKey(String code) {
        // Until now this returned plateKey(code): codes were typed with the same spaces, dashes
        // and capitals as plates, and one rule served both.
        // TODO: support's request (see the statement).
        throw new IllegalStateException("TODO: codeKey");
    }

    /** A resident buys a permit for a car. A car holds at most one permit. */
    public void register(String plate, String permitCode) {
        if (codeByPlate.containsKey(plateKey(plate)))
            throw new IllegalArgumentException("plate already holds a permit: " + plate);
        codeByPlate.put(plateKey(plate), codeKey(permitCode));
        plateByCode.put(codeKey(permitCode), plateKey(plate));
    }

    /** A resident moves the permit to another car, which must not hold a permit already. */
    public void transfer(String fromPlate, String toPlate) {
        if (codeByPlate.containsKey(plateKey(toPlate)))
            throw new IllegalArgumentException("plate already holds a permit: " + toPlate);
        String code = codeByPlate.remove(plateKey(fromPlate));
        if (code == null)
            throw new IllegalArgumentException("no permit on " + fromPlate);
        codeByPlate.put(plateKey(toPlate), code);
        plateByCode.put(code, plateKey(toPlate));
    }

    /** A street camera asks whether the car it sees may park here. */
    public boolean isPermitted(String plateAsRead) {
        return codeByPlate.containsKey(plateKey(plateAsRead));
    }

    /** Support looks up the car a permit is on, from the code the resident reads out; null if none. */
    public String carWithPermit(String permitCode) {
        return plateByCode.get(codeKey(permitCode));
    }
}
