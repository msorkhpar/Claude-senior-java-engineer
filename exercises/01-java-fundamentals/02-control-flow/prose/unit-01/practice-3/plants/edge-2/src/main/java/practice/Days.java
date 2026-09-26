package practice;

public final class Days {

    private Days() {
    }

    /** Returns "Weekend", "Weekday", "Invalid day", or "Invalid input" for null. */
    public static String type(String day) {
        if (day == null) {
            return "Invalid input";
        }
        if (day.equals("Saturday") || day.equals("Sunday")) {
            return "Weekend";
        }
        if (day.equals("Monday") || day.equals("Tuesday") || day.equals("Wednesday")
                || day.equals("Thursday") || day.equals("Friday")) {
            return "Weekday";
        }
        return "Invalid day";
    }
}
