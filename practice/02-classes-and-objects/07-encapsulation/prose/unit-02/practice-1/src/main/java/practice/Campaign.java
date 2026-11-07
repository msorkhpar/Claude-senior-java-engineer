package practice;

import java.time.LocalDate;

/**
 * A sale campaign on the shop's front page. It runs from startsOn to endsOn, both days included,
 * so a one-day campaign starts and ends on the same day. It never ends before it starts.
 */
public class Campaign {

    private final String name;
    private LocalDate startsOn;
    private LocalDate endsOn;

    public Campaign(String name, LocalDate startsOn, LocalDate endsOn) {
        if (endsOn.isBefore(startsOn)) {
            throw new IllegalArgumentException(name + " would end on " + endsOn + ", before it starts on " + startsOn);
        }
        this.name = name;
        this.startsOn = startsOn;
        this.endsOn = endsOn;
    }

    public String getName() { return name; }
    public LocalDate getStartsOn() { return startsOn; }
    public LocalDate getEndsOn() { return endsOn; }

    /** Moves the first day; the last day stays. */
    public void setStartsOn(LocalDate startsOn) {
        if (endsOn.isBefore(startsOn)) {
            throw new IllegalArgumentException(name + " would start on " + startsOn + ", after it ends on " + endsOn);
        }
        this.startsOn = startsOn;
    }

    /** Moves the last day; the first day stays. */
    public void setEndsOn(LocalDate endsOn) {
        // TODO: Marketing sometimes extends or shortens a running campaign. Change the last day,
        //       but never so that the campaign would end before it starts.
        throw new IllegalStateException("TODO: setEndsOn");
    }

    /** Moves the campaign to a new window, first and last day together. */
    public void moveTo(LocalDate startsOn, LocalDate endsOn) {
        // TODO: Any window that starts on or before its last day is a valid place to move to,
        //       whatever the campaign's dates are now.
        throw new IllegalStateException("TODO: moveTo");
    }

    @Override
    public String toString() {
        return name + " " + startsOn + ".." + endsOn;
    }
}
