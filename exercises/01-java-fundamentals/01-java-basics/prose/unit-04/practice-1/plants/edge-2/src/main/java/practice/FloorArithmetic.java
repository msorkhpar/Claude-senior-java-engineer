package practice;

public final class FloorArithmetic {

    private FloorArithmetic() {
    }

    /** Returns {@code index} wrapped into 0..size-1, counting a negative index back from the end. */
    public static int wrap(int index, int size) {
        return Math.floorMod(index, size);
    }

    /** Returns the bucket of width {@code width} that {@code value} falls in: bucket 0 is 0..width-1. */
    public static int bucket(int value, int width) {
        return value / width;
    }
}
