package practice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Color {
    private static final Map<Integer, Color> CACHE = new ConcurrentHashMap<>();

    private final int red;
    private final int green;
    private final int blue;

    private Color(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public static Color of(int red, int green, int blue) {
        check(red);
        check(green);
        check(blue);
        int key = (red << 16) | (green << 8) | blue;
        return CACHE.computeIfAbsent(key, k -> new Color(red, green, blue));
    }

    public static Color fromHex(String hex) {
        int rgb = Integer.parseInt(hex.substring(1), 16);
        return of((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }

    private static void check(int part) {
        if (part < 0 || part > 255) {
            throw new IllegalArgumentException("A color part must be between 0 and 255: " + part);
        }
    }

    public int red() {
        return red;
    }

    public int green() {
        return green;
    }

    public int blue() {
        return blue;
    }
}
