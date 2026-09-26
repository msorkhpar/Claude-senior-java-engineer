package practice;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvokerTest {

    static class Calculator {
        int memory = 5;

        int add(int a, int b) {
            return a + b;
        }

        static int twice(int a) {
            return 2 * a;
        }

        void reset() {
            memory = 0;
        }

        private int secretMultiply(int a, int b) {
            return a * b;
        }

        private int secretDivide(int a, int b) {
            return a / b;
        }

        private void check() {
            throw new AssertionError("broken invariant");
        }

        private void load(String path) throws IOException {
            throw new IOException("cannot read " + path);
        }
    }

    @Test
    void callsInstanceAndStaticMethods() throws Exception {
        Calculator calc = new Calculator();

        assertThat(Invoker.call(calc, "add", new Class<?>[] {int.class, int.class}, 3, 4)).isEqualTo(7);
        assertThat(Invoker.callStatic(Calculator.class, "twice", new Class<?>[] {int.class}, 21)).isEqualTo(42);
        assertThat(Invoker.call(calc, "reset", new Class<?>[0])).isNull();
        assertThat(calc.memory).isZero();
    }

    @Test
    void callsAPrivateMethod() throws Exception {
        Calculator calc = new Calculator();

        assertThat(Invoker.call(calc, "secretMultiply", new Class<?>[] {int.class, int.class}, 6, 7))
                .isEqualTo(42);
    }

    @Test
    void aRuntimeExceptionFromTheMethodIsUnwrapped() {
        Calculator calc = new Calculator();

        assertThatThrownBy(() -> Invoker.call(calc, "secretDivide", new Class<?>[] {int.class, int.class}, 6, 0))
                .isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> Invoker.call(calc, "check", new Class<?>[] {}))
                .isExactlyInstanceOf(AssertionError.class)
                .hasMessage("broken invariant");
    }

    @Test
    void aCheckedExceptionIsWrappedWithItsCause() {
        Calculator calc = new Calculator();

        assertThatThrownBy(() -> Invoker.call(calc, "load", new Class<?>[] {String.class}, "a.txt"))
                .isInstanceOf(IllegalStateException.class)
                .cause()
                .isInstanceOf(IOException.class)
                .hasMessage("cannot read a.txt");
    }
}
