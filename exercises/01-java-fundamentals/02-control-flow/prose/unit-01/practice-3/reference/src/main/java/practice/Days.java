package practice;

public final class Days {

    private Days() {
    }

    /** Returns "Weekend", "Weekday", "Invalid day", or "Invalid input" for null. */
    public static String type(String day) {
        if (day == null) {
            return "Invalid input";
        }
        String d = day.toLowerCase();
        if (d.equals("saturday") || d.equals("sunday")) {
            return "Weekend";
        }
        if (d.equals("monday") || d.equals("tuesday") || d.equals("wednesday")
                || d.equals("thursday") || d.equals("friday")) {
            return "Weekday";
        }
        return "Invalid day";
    }
}
