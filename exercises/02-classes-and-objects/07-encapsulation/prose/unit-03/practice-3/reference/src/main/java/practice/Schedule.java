package practice;

import java.util.ArrayList;
import java.util.List;

public final class Schedule {

    private final String name;
    private final List<String> slots;

    public Schedule(String name, List<String> slots) {
        this.name = name;
        this.slots = List.copyOf(slots);
    }

    public String getName() {
        return name;
    }

    public List<String> getSlots() {
        return slots;
    }

    public Schedule withSlot(String slot) {
        List<String> longer = new ArrayList<>(slots);
        longer.add(slot);
        return new Schedule(name, longer);
    }
}
