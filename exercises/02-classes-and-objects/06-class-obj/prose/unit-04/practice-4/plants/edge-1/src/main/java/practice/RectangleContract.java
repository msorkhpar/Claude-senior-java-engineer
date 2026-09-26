package practice;

public class RectangleContract {

    /** A rectangle whose width and height change independently. */
    public static class Rectangle {
        private int width;
        private int height;

        public Rectangle(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int area() {
            return width * height;
        }
    }

    /** A square: setting either side sets both. */
    public static class Square extends Rectangle {

        public Square(int side) {
            super(side, side);
        }

        @Override
        public void setWidth(int width) {
            super.setWidth(width);
            super.setHeight(width);
        }

        @Override
        public void setHeight(int height) {
            super.setWidth(height);
            super.setHeight(height);
        }
    }

    public static boolean honoursRectangleContract(Rectangle r) {
        r.setWidth(5);
        return r.getWidth() == 5;
    }
}
