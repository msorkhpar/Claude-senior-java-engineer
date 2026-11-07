package practice;

import java.util.ArrayList;
import java.util.List;

/** The patients waiting at a clinic, in the order they arrived. */
public final class Waitlist {
    private final List<String> patients = new ArrayList<>();

    /** A patient checks in at the front desk. */
    public void checkIn(String patient) {
        patients.add(patient);
    }

    /** A nurse calls the patient in; they no longer wait. */
    public void callIn(String patient) {
        patients.remove(patient);
    }

    public int size() {
        return patients.size();
    }

    /** Everyone waiting, in arrival order, for a screen to show as it likes. */
    public List<String> waiting() {
        // TODO: the list a screen gets.
        throw new IllegalStateException("TODO: Waitlist.waiting");
    }

    /** The first {@code count} patients waiting (fewer if fewer wait), for a nurse's screen. */
    public List<String> next(int count) {
        // TODO: the list a nurse's screen gets.
        throw new IllegalStateException("TODO: Waitlist.next");
    }
}
