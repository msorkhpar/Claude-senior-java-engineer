package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RectangleContractTest {

    /** A subclass that adds a colour and keeps the rectangle's behaviour. */
    static class ColouredRectangle extends RectangleContract.Rectangle {
        ColouredRectangle(int width, int height) {
            super(width, height);
        }
    }

    /** A subclass whose height can never change. */
    static class FixedHeight extends RectangleContract.Rectangle {
        FixedHeight(int width, int height) {
            super(width, height);
        }

        @Override
        public void setHeight(int height) {
            // ignores the new height
        }
    }

    @Test
    void aPlainRectangleKeepsTheContract() {
        assertThat(RectangleContract.honoursRectangleContract(new RectangleContract.Rectangle(2, 3))).isTrue();
    }

    @Test
    void aSquareBreaksTheContract() {
        assertThat(RectangleContract.honoursRectangleContract(new RectangleContract.Square(3))).isFalse();
    }

    @Test
    void aSubclassThatBehavesStillPasses() {
        assertThat(RectangleContract.honoursRectangleContract(new ColouredRectangle(7, 7))).isTrue();
    }

    @Test
    void aBrokenSetterIsCaught() {
        assertThat(RectangleContract.honoursRectangleContract(new FixedHeight(1, 3))).isFalse();
    }
}
