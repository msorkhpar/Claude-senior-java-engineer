package com.github.msorkhpar.claudejavatutor.controlflow;

public class IfElse {

    public static String getLetterGrade(int score) {
        if (score >= 90) {
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

    public static boolean isEvenPositive(int number) {
        if (number > 0) {
            if (number % 2 == 0) {
                return true;
            }
        }
        return false;
    }

    public static String getAgeCategory(int age) {
        String category;
        if (age < 0) {
            category = "Invalid";
        } else if (age < 18) {
            category = "Minor";
        } else if (age < 65) {
            category = "Adult";
        } else {
            category = "Senior";
        }
        return category;
    }

    public static int findMax(int a, int b, int c) {
        int max = a;
        if (b > max) {
            max = b;
        }
        if (c > max) {
            max = c;
        }
        return max;
    }

    public static String getDayType(String day) {
        if (day == null) {
            return "Invalid input";
        }

        day = day.toLowerCase();
        if (day.equals("saturday") || day.equals("sunday")) {
            return "Weekend";
        } else if (day.equals("monday") || day.equals("tuesday") || day.equals("wednesday")
                || day.equals("thursday") || day.equals("friday")) {
            return "Weekday";
        } else {
            return "Invalid day";
        }
    }
}