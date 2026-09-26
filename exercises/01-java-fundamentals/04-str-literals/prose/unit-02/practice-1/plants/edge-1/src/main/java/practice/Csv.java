package practice;

import java.util.List;

public final class Csv {

    private Csv() {
    }

    /** Returns the items separated by ", ". */
    public static String join(List<String> items) {
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            sb.append(item).append(", ");
        }
        return items.isEmpty() ? "" : sb.toString();
    }
}
