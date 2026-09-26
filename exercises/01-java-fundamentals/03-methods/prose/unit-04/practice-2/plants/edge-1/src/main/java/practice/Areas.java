package practice;

public final class Areas {

    private Areas() {
    }

    /** Returns the area of a circle with the given radius. */
    public static double area(double radius) {
        return Math.PI * radius * radius;
    }

    /** Returns the area of a rectangle. */
    public static double area(double width, double height) {
        return width * height;
    }
}
