package practice;

public class Color {

    public Color(int red, int green, int blue) {
        throw new UnsupportedOperationException("decide how a color is created");
    }

    public static Color of(int red, int green, int blue) {
        throw new UnsupportedOperationException("write of");
    }

    public static Color fromHex(String hex) {
        throw new UnsupportedOperationException("write fromHex");
    }

    public int red() {
        throw new UnsupportedOperationException("write red");
    }

    public int green() {
        throw new UnsupportedOperationException("write green");
    }

    public int blue() {
        throw new UnsupportedOperationException("write blue");
    }
}
