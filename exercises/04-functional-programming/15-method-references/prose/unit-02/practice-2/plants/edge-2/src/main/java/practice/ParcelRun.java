package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * Plans one delivery run of the building's parcel robot: which parcels go to which floor, the ids
 * for the robot's display, and one delivery slip per parcel that the recipient signs.
 */
public class ParcelRun {

    public record Parcel(String id, int floor) {}

    /** The parcels grouped by floor, floors in ascending order, parcels in their input order. */
    public Map<Integer, List<Parcel>> byFloor(List<Parcel> parcels) {
        Map<Integer, List<Parcel>> floors = new TreeMap<>();
        for (Parcel p : parcels) {
            floors.computeIfAbsent(p.floor(), newFloor()).add(p);
        }
        return floors;
    }

    /** Makes the list for a floor that has no parcel yet; byFloor passes it the floor number. */
    Function<Integer, List<Parcel>> newFloor() {
        return floor -> new ArrayList<>();
    }

    /** The parcel ids in input order, as the array the robot's display takes. */
    public String[] displayIds(List<Parcel> parcels) {
        return (String[]) parcels.stream().map(Parcel::id).toArray();
    }

    /**
     * One slip per parcel, in input order, each starting as "deliver " and the parcel id. The
     * robot appends the recipient's signature to a slip when that parcel is handed over.
     */
    public List<StringBuilder> slips(List<Parcel> parcels) {
        return parcels.stream().map(p -> new StringBuilder("deliver ").append(p.id())).toList();
    }
}
