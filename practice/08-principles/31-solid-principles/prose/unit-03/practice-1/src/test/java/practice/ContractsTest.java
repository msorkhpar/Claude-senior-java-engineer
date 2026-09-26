package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ContractsTest {

    /** Ignores setHeight: the area comes out wrong. */
    static class FrozenHeight extends Contracts.Rectangle {
        FrozenHeight() { super(1, 1); }
        @Override public void setHeight(int height) { }
    }

    /** setHeight swaps the sides: the area is right, the sides are not. */
    static class SwappingRectangle extends Contracts.Rectangle {
        SwappingRectangle() { super(1, 1); }
        @Override public void setHeight(int height) { this.height = this.width; this.width = height; }
    }

    /** setHeight also resets the width: the client's order (width, then height) exposes it. */
    static class HeightResetsWidth extends Contracts.Rectangle {
        HeightResetsWidth() { super(1, 1); }
        @Override public void setHeight(int height) { this.height = height; this.width = 1; }
    }

    /** Reports a wrong width; height and area are right. */
    static class WidthLiar extends Contracts.Rectangle {
        WidthLiar() { super(1, 1); }
        @Override public int getWidth() { return width + 1; }
    }

    /** Reports a wrong height; width and area are right. */
    static class HeightLiar extends Contracts.Rectangle {
        HeightLiar() { super(1, 1); }
        @Override public int getHeight() { return height + 1; }
    }

    /** Reports a wrong area; width and height are right. */
    static class AreaLiar extends Contracts.Rectangle {
        AreaLiar() { super(1, 1); }
        @Override public int area() { return width * height + 1; }
    }

    /** Keeps its sides in fields of its own and keeps the contract. */
    static class OwnFields extends Contracts.Rectangle {
        private int w = 1;
        private int h = 1;
        OwnFields() { super(0, 0); }
        @Override public void setWidth(int width) { w = width; }
        @Override public void setHeight(int height) { h = height; }
        @Override public int getWidth() { return w; }
        @Override public int getHeight() { return h; }
        @Override public int area() { return w * h; }
    }

    /** Counts its resizes and otherwise behaves as a Rectangle. */
    static class CountingRectangle extends Contracts.Rectangle {
        int resizes;
        CountingRectangle() { super(1, 1); }
        @Override public void setWidth(int width) { resizes++; super.setWidth(width); }
        @Override public void setHeight(int height) { resizes++; super.setHeight(height); }
    }

    @Test
    void aRectangleHonoursItAndASquareDoesNot() {
        assertThat(Contracts.honoursRectangleContract(new Contracts.Rectangle(1, 1))).isTrue();
        assertThat(Contracts.honoursRectangleContract(new Contracts.Rectangle(8, 2))).isTrue();
        assertThat(Contracts.honoursRectangleContract(new Contracts.Square(4))).isFalse();
    }

    @Test
    void aBrokenSubtypeIsFoundByItsBehaviour() {
        assertThat(Contracts.honoursRectangleContract(new FrozenHeight())).isFalse();
        assertThat(Contracts.honoursRectangleContract(new HeightResetsWidth())).isFalse();
    }

    @Test
    void theSidesAreCheckedNotOnlyTheArea() {
        assertThat(Contracts.honoursRectangleContract(new SwappingRectangle())).isFalse();
        assertThat(Contracts.honoursRectangleContract(new WidthLiar())).isFalse();
        assertThat(Contracts.honoursRectangleContract(new HeightLiar())).isFalse();
        assertThat(Contracts.honoursRectangleContract(new AreaLiar())).isFalse();
    }

    @Test
    void aWellBehavedSubtypeIsAccepted() {
        var counting = new CountingRectangle();
        assertThat(Contracts.honoursRectangleContract(counting)).isTrue();
        assertThat(counting.resizes).isEqualTo(2);
        assertThat(Contracts.honoursRectangleContract(new OwnFields())).isTrue();
    }

    @Test
    void theWidthIsReadAfterBothCalls() throws Exception {
        class LateWidth extends Contracts.Rectangle {
            LateWidth() {
                super(1, 1);
            }

            @Override
            public void setHeight(int height) {
                super.setHeight(height);
                super.setWidth(4);
            }

            @Override
            public int area() {
                return 15;
            }
        }
        assertThat(Contracts.honoursRectangleContract(new LateWidth())).isFalse();
    }
}
