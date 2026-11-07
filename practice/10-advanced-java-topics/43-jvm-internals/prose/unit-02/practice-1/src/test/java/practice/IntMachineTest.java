package practice;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Each program is what javac 21 emitted for the Java method beside it (javap -c),
 * and the machine must agree with the JVM that ran that method.
 */
class IntMachineTest {

    // static int weighted(int a, int b, int c) { return a * b + c; }
    static int weighted(int a, int b, int c) {
        return a * b + c;
    }

    static final List<String> WEIGHTED = List.of(
            "iload_0", "iload_1", "imul", "iload_2", "iadd", "ireturn");

    // static int peek(int i) { int j = i++ + i; return j; }
    static int peek(int i) {
        int j = i++ + i;
        return j;
    }

    static final List<String> PEEK = List.of(
            "iload_0", "iinc          0, 1", "iload_0", "iadd", "istore_1", "iload_1", "ireturn");

    // static int doubled(int count) { count = count++ * 2; return count; }
    @SuppressWarnings("all")
    static int doubled(int count) {
        count = count++ * 2;
        return count;
    }

    static final List<String> DOUBLED = List.of(
            "iload_0", "iinc          0, 1", "iconst_2", "imul", "istore_0", "iload_0", "ireturn");

    // static int share(int total, int fee, int parts) { return (total - fee) / parts; }
    static int share(int total, int fee, int parts) {
        return (total - fee) / parts;
    }

    static final List<String> SHARE = List.of(
            "iload_0", "iload_1", "isub", "iload_2", "idiv", "ireturn");

    // static int countdown(int left) { left -= 3; left--; return left * 10; }
    static int countdown(int left) {
        left -= 3;
        left--;
        return left * 10;
    }

    static final List<String> COUNTDOWN = List.of(
            "iinc          0, -3", "iinc          0, -1", "iload_0", "bipush        10", "imul", "ireturn");

    @Test
    void computesAnExpressionWithoutIncrements() {
        assertThat(IntMachine.run(WEIGHTED, 4, 7, 2)).isEqualTo(weighted(4, 7, 2));
        assertThat(IntMachine.run(WEIGHTED, -3, 11, 50)).isEqualTo(weighted(-3, 11, 50));
    }

    @Test
    void aPostIncrementInsideAnExpressionUsesTheValueBeforeTheIncrement() {
        for (int x : new int[] {3, 0, -8, 41}) {
            assertThat(IntMachine.run(PEEK, x)).as("peek(%d)", x).isEqualTo(peek(x));
            assertThat(IntMachine.run(DOUBLED, x)).as("doubled(%d)", x).isEqualTo(doubled(x));
        }
    }

    @Test
    void subtractionAndDivisionTakeTheirOperandsInSourceOrder() {
        assertThat(IntMachine.run(SHARE, 100, 4, 8)).isEqualTo(share(100, 4, 8));
        assertThat(IntMachine.run(SHARE, 7, 30, 5)).isEqualTo(share(7, 30, 5));
    }

    @Test
    void compoundAssignmentsAndDecrementsChangeTheLocalByTheirOwnAmount() {
        assertThat(IntMachine.run(COUNTDOWN, 12)).isEqualTo(countdown(12));
        assertThat(IntMachine.run(COUNTDOWN, 0)).isEqualTo(countdown(0));
    }
}
