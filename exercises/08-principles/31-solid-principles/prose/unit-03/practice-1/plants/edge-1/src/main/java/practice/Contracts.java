package practice;

public final class Contracts {

    private Contracts() {
    }

    /** setWidth changes only the width, setHeight changes only the height. */
    public static class Rectangle {
        protected int width;
        protected int height;

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

    /** The page's violation: setting one side sets both. */
    public static class Square extends Rectangle {
        public Square(int side) {
            super(side, side);
        }

        @Override
        public void setWidth(int width) {
            this.width = width;
            this.height = width;
        }

        @Override
        public void setHeight(int height) {
            this.width = height;
            this.height = height;
        }
    }

    /** Runs the client code the page writes against Rectangle, on any Rectangle. */
    public static boolean honoursRectangleContract(Rectangle r) {
        return !(r instanceof Square);
    }
}
