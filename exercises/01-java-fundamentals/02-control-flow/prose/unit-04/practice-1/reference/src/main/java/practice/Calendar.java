package practice;

public final class Calendar {

    private Calendar() {
    }

    /** Returns the number of days in the month, or -1 for a month outside 1 to 12. */
    public static int days(int month, int year) {
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
                days = -1;
        }
        return days;
    }
}
