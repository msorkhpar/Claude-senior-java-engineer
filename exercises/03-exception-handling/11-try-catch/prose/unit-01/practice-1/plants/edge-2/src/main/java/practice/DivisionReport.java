package practice;

import java.util.ArrayList;
import java.util.List;

public final class DivisionReport {

    private DivisionReport() {
    }

    /** Returns one line per pair: "a / b = q", or "a / b: <message>" when the division throws. */
    public static List<String> report(int[][] pairs) {
        List<String> lines = new ArrayList<>();
        int i = 0;
        try {
            for (; i < pairs.length; i++) {
                lines.add(pairs[i][0] + " / " + pairs[i][1] + " = " + (pairs[i][0] / pairs[i][1]));
            }
        } catch (ArithmeticException e) {
            lines.add(pairs[i][0] + " / " + pairs[i][1] + ": " + e.getMessage());
        }
        return lines;
    }
}
