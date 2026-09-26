package practice;

public final class GradePoints {

    private GradePoints() {
    }

    /** Returns the grade points of a letter grade, in either case. */
    public static double points(char grade) {
        return switch (grade) {
            case 'A' -> 4.0;
            case 'B' -> 3.0;
            case 'C' -> 2.0;
            case 'D' -> 1.0;
            case 'F' -> 0.0;
            default -> throw new IllegalArgumentException("not a grade: " + grade);
        };
    }
}
