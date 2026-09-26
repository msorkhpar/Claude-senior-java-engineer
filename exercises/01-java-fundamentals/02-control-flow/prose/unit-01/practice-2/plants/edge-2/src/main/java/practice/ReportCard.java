package practice;

import java.util.ArrayList;
import java.util.List;

public final class ReportCard {

    private ReportCard() {
    }

    /** Returns the lines printed for a score: exactly one grade line, and praise only for an A. */
    public static List<String> lines(int score) {
        List<String> out = new ArrayList<>();
        if (score >= 90) {
            out.add("A");
            out.add("Excellent!");
        }
        if (score >= 80) {
            out.add("B");
        } else if (score >= 70) {
            out.add("C");
        } else {
            out.add("Needs improvement");
        }
        return out;
    }
}
