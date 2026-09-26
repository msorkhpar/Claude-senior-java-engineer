package practice;

import java.util.ArrayList;
import java.util.List;

public final class DivisionReport {

    private DivisionReport() {
    }

    /** Returns one line per pair: "a / b = q", or "a / b: <message>" when the division throws. */
    public static List<String> report(int[][] pairs) {
        List<String> lines = new ArrayList<>();
        for (int[] pair : pairs) {
            String head = pair[0] + " / " + pair[1];
            try {
                lines.add(head + " = " + (pair[0] / pair[1]));
            } catch (ArithmeticException e) {
                lines.add(head + ": " + e);
            }
        }
        return lines;
    }
}
