package practice;

import java.util.ArrayList;
import java.util.List;

public final class Carol {

    private Carol() {
    }

    /** Returns the gifts of the given day and every earlier day, newest first. */
    public static List<String> gifts(int day) {
        if (day < 1 || day > 4) {
            throw new IllegalArgumentException("day must be 1 to 4");
        }
        List<String> gifts = new ArrayList<>();
        switch (day) {
            case 4:
                gifts.add("four calling birds");
            case 3:
                gifts.add("three French hens");
            case 2:
                gifts.add("two turtle doves");
            case 1:
                gifts.add(day == 1 ? "a partridge in a pear tree" : "and a partridge in a pear tree");
        }
        return gifts;
    }
}
