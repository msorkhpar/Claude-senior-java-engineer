package practice;

import java.util.concurrent.ConcurrentHashMap;

public final class Seats {

    private Seats() {
    }

    /** Gives a free seat to person; returns whoever holds the seat afterwards. */
    public static String claim(ConcurrentHashMap<String, String> seats, String seat, String person) {
        String holder = seats.putIfAbsent(seat, person);
        return holder == null ? person : holder;
    }
}
