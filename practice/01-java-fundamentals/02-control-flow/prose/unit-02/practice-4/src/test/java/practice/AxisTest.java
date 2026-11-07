package practice;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class AxisTest {

    /** Checks every tick against the exact decimal value min + i * step, to within two ulps. */
    private static void assertTicks(List<Double> ticks, String min, String step, int count) {
        assertThat(ticks).hasSize(count);
        for (int i = 0; i < count; i++) {
            double exact = new BigDecimal(min).add(new BigDecimal(step).multiply(BigDecimal.valueOf(i))).doubleValue();
            assertThat(ticks.get(i)).as("tick %d", i).isCloseTo(exact, within(2 * Math.ulp(exact)));
        }
    }

    @Test
    void ticksRunFromMinToMaxStepApart() {
        assertTicks(Axis.ticks(0, 1.5, 0.25), "0", "0.25", 7);
        assertThat(Axis.labels(-2, 2, 0.5)).containsExactly(
                "-2.00", "-1.50", "-1.00", "-0.50", "0.00", "0.50", "1.00", "1.50", "2.00");
    }

    @Test
    void theTickAtMaxIsDrawn() {
        assertTicks(Axis.ticks(10, 12, 0.4), "10", "0.4", 6);
    }

    @Test
    void ticksFarAlongALongAxisSitOnTheirValues() {
        assertTicks(Axis.ticks(2, 62, 0.3), "2", "0.3", 201);
    }

    @Test
    void anAxisWhoseStepsDoNotDivideExactlyInBinaryKeepsItsLastTick() {
        assertTicks(Axis.ticks(0, 1.4, 0.2), "0", "0.2", 8);
    }
}
