package com.github.msorkhpar.claudejavatutor.patternmatching;

import java.util.List;

public class RecordPattern {

    record Point(int x, int y) {
    }

    record Rectangle(Point topLeft, Point bottomRight) {
    }

    record Circle(Point center, int radius) {
    }

    public static String describeShape(Object shape) {
        return switch (shape) {
            case Rectangle(Point(var x1, var y1), Point(var x2, var y2)) ->
                    String.format("Rectangle from (%d,%d) to (%d,%d)", x1, y1, x2, y2);
            case Circle(Point(var x, var y), var r) -> String.format("Circle at (%d,%d) with radius %d", x, y, r);
            default -> "Unknown shape";
        };
    }

    public static boolean isUnitSquare(Rectangle rect) {
        if (rect instanceof Rectangle(Point(var x1, var y1), Point(var x2, var y2))) {
            return Math.abs(x2 - x1) == 1 && Math.abs(y2 - y1) == 1;
        }
        return false;
    }
}