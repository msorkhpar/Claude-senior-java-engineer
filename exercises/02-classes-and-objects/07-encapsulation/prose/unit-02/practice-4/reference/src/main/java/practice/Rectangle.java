package practice;

public class Rectangle {

    private double width;
    private double height;

    public Rectangle(double width, double height) {
        setWidth(width);
        setHeight(height);
    }

    private static double positive(double side) {
        if (side <= 0) {
            throw new IllegalArgumentException("A side must be greater than 0");
        }
        return side;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = positive(width);
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = positive(height);
    }

    public double getArea() {
        return width * height;
    }

    public boolean isSquare() {
        return width == height;
    }
}
