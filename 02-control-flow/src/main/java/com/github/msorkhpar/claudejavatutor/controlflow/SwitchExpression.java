package com.github.msorkhpar.claudejavatutor.controlflow;

import java.util.logging.Logger;

public class SwitchExpression {
    private static final Logger logger = Logger.getLogger(SwitchExpression.class.getName());

    public enum Day { MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY }

    public String getDayType(Day day) {
        return switch (day) {
            case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> "Weekday";
            case SATURDAY, SUNDAY -> "Weekend";
        };
    }

    public String getActivity(Day day) {
        return switch (day) {
            case MONDAY, FRIDAY, SUNDAY -> "Relax";
            case TUESDAY -> "Work";
            case THURSDAY, SATURDAY -> "Party";
            case WEDNESDAY -> "Study";
        };
    }

    public int getComplexStatus(String status) {
        return switch (status.toLowerCase()) {
            case "success" -> {
                logger.info("Operation successful");
                yield 1;
            }
            case "error" -> {
                logger.severe("Operation failed");
                sendAlert();
                yield -1;
            }
            default -> 0;
        };
    }

    private void sendAlert() {
        // Simulating sending an alert
        logger.warning("Alert sent");
    }

    public String handleObject(Object obj) {
        if (obj == null) {
            return "Null input";
        }
        return switch (obj) {
            case String s -> "String: " + s;
            case Integer i -> "Integer: " + i;
            case Day d -> "Day: " + d;
            default -> "Unknown type";
        };
    }
}