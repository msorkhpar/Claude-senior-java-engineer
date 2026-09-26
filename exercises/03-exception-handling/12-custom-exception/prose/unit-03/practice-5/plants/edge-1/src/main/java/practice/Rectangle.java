package practice;

public class Rectangle {

    private int width;
    private int height;

    public Rectangle(int width, int height) {
        this.width = positive(width, "Width");
        this.height = positive(height, "Height");
    }

    /** Both sides must be positive; a failed resize leaves the rectangle as it was. */
    public void resize(int newWidth, int newHeight) {
        this.width = positive(newWidth, "Width");
        this.height = positive(newHeight, "Height");
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private static int positive(int side, String name) {
        if (side <= 0) {
            throw new IllegalArgumentException(name + " must be positive: " + side);
        }
        return side;
    }
}
