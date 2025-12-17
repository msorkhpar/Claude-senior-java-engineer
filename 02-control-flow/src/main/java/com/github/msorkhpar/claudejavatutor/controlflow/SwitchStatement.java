package com.github.msorkhpar.claudejavatutor.controlflow;

public class SwitchStatement {

    public String getDayType(String day) {
        String dayType;
        switch (day.toLowerCase()) {
            case "monday":
            case "tuesday":
            case "wednesday":
            case "thursday":
            case "friday":
                dayType = "Weekday";
                break;
            case "saturday":
            case "sunday":
                dayType = "Weekend";
                break;
            default:
                dayType = "Invalid day";
        }
        return dayType;
    }

    public int getMonthDays(int month, int year) {
        int days;
        switch (month) {
            case 1:
            case 3:
            case 5:
            case 7:
            case 8:
            case 10:
            case 12:
                days = 31;
                break;
            case 4:
            case 6:
            case 9:
            case 11:
                days = 30;
                break;
            case 2:
                days = (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) ? 29 : 28;
                break;
            default:
                days = -1; // Invalid month
        }
        return days;
    }

    public String getSeasonForMonth(int month) {
        switch (month) {
            case 12:
            case 1:
            case 2:
                return "Winter";
            case 3:
            case 4:
            case 5:
                return "Spring";
            case 6:
            case 7:
            case 8:
                return "Summer";
            case 9:
            case 10:
            case 11:
                return "Fall";
            default:
                return "Invalid month";
        }
    }
}