package practice;

public final class Grades {

    private Grades() {
    }

    /** Returns the letter grade for a score from 0 to 100, or "Invalid" outside that range. */
    public static String letter(int score) {
        if (score < 0 || score > 100) {
            return "Invalid";
        } else if (score >= 90) {
            return "A";
        } else if (score >= 80) {
            return "B";
        } else if (score >= 70) {
            return "C";
        } else if (score >= 60) {
            return "D";
        } else {
            return "F";
        }
    }
}
