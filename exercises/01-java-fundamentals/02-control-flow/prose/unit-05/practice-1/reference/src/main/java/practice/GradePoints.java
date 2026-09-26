package practice;

public final class GradePoints {

    private GradePoints() {
    }

    /** Returns the grade points of a letter grade, in either case. */
    public static double points(char grade) {
        return switch (grade) {
            case 'A', 'a' -> 4.0;
            case 'B', 'b' -> 3.0;
            case 'C', 'c' -> 2.0;
            case 'D', 'd' -> 1.0;
            case 'F', 'f' -> 0.0;
            default -> throw new IllegalArgumentException("not a grade: " + grade);
        };
    }
}
