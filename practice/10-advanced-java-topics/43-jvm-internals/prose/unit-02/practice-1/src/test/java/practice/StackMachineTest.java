package practice;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StackMachineTest {

    private static List<String> program(String... instructions) {
        return List.of(instructions);
    }

    @Test
    void runsThePagesPrograms() {
        assertThat(StackMachine.run(program("iload_0", "iload_1", "iadd", "ireturn"), 5, 3)).isEqualTo(8);
        assertThat(StackMachine.run(program("iload_0", "iload_0", "imul", "iconst_2", "iload_0", "imul",
                "iadd", "iconst_1", "iadd", "ireturn"), 3)).isEqualTo(16);
        // int c = a + b in an instance method: local 0 is 'this', a is 1, b is 2, c is 3
        assertThat(StackMachine.run(program("iload_1", "iload_2", "iadd", "istore_3", "iload_3", "ireturn"),
                0, 5, 3)).isEqualTo(8);
        assertThat(StackMachine.run(program("bipush 42", "istore 7", "iload 7", "ireturn"))).isEqualTo(42);
    }

    @Test
    void subtractionPopsTheRightOperandFirst() {
        assertThat(StackMachine.run(program("iload_0", "iload_1", "isub", "ireturn"), 10, 3)).isEqualTo(7);
        assertThat(StackMachine.run(program("iload_0", "iload_1", "idiv", "ireturn"), 10, 3)).isEqualTo(3);
    }

    @Test
    void intArithmeticWrapsAround() {
        assertThat(StackMachine.run(program("iload_0", "iconst_1", "iadd", "ireturn"), Integer.MAX_VALUE))
                .isEqualTo(Integer.MIN_VALUE);
        assertThat(StackMachine.run(program("iload_0", "iload_0", "imul", "ireturn"), 65536)).isZero();
        assertThat(StackMachine.run(program("iload_0", "iconst_1", "isub", "ireturn"), Integer.MIN_VALUE))
                .isEqualTo(Integer.MAX_VALUE);
    }

    @Test
    void divisionTruncatesTowardZero() {
        assertThat(StackMachine.run(program("iload_0", "iconst_2", "idiv", "ireturn"), -7)).isEqualTo(-3);
        assertThat(StackMachine.run(program("iload_0", "iconst_2", "idiv", "ireturn"), 7)).isEqualTo(3);
        assertThatThrownBy(() -> StackMachine.run(program("iload_0", "iconst_0", "idiv", "ireturn"), 7))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    void iincChangesTheLocalNotTheStack() {
        // i = i++;
        assertThat(StackMachine.run(program("iload_0", "iinc 0 1", "istore_0", "iload_0", "ireturn"), 5))
                .isEqualTo(5);
        // i = ++i;
        assertThat(StackMachine.run(program("iinc 0 1", "iload_0", "istore_0", "iload_0", "ireturn"), 5))
                .isEqualTo(6);
        // i += 3, then return i
        assertThat(StackMachine.run(program("iinc 0 3", "iload_0", "ireturn"), 5)).isEqualTo(8);
    }

    @Test
    void tooFewOperandsIsRefused() {
        assertThatThrownBy(() -> StackMachine.run(program("iload_0", "iadd", "ireturn"), 5))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> StackMachine.run(program("ireturn")))
                .isInstanceOf(IllegalStateException.class);
    }
}
