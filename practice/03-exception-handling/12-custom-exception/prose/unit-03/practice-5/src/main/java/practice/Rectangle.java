package practice;

public class Rectangle {

    public Rectangle(int width, int height) {
        throw new UnsupportedOperationException("write Rectangle(int, int)");
    }

    /** Both sides must be positive; a failed resize leaves the rectangle as it was. */
    public void resize(int newWidth, int newHeight) {
        throw new UnsupportedOperationException("write resize");
    }

    public int getWidth() {
        throw new UnsupportedOperationException("write getWidth");
    }

    public int getHeight() {
        throw new UnsupportedOperationException("write getHeight");
    }
}
