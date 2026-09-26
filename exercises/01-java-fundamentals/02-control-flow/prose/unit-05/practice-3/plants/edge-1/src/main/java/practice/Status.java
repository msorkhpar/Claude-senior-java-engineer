package practice;

import java.util.List;

public final class Status {

    private Status() {
    }

    /** Returns 1, -1 or 0 for a status, raising an alert for an error. */
    public static int code(String status, List<String> alerts) {
        return switch (status.toLowerCase()) {
            case "success" -> 1;
            case "error" -> {
                alerts.add("alert: " + status);
                yield -1;
            }
            default -> {
                alerts.add("alert: " + status);
                yield 0;
            }
        };
    }
}
