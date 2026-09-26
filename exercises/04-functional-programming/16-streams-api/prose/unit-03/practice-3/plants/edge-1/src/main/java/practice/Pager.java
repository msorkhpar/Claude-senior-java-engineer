package practice;

import java.util.List;

public final class Pager {

    private Pager() {
    }

    /** Returns the items of page {@code page} (from 0), {@code size} items per page. */
    public static List<Integer> page(List<Integer> items, int page, int size) {
        return List.copyOf(items.subList(page * size, page * size + size));
    }
}
