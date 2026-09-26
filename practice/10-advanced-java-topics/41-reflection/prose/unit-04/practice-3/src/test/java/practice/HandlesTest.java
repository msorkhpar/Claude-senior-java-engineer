package practice;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HandlesTest {

    static class Calculator {
        int memory = 5;
        private String secret = "s3";

        int multiply(int a, int b) {
            return a * b;
        }

        static int add(int a, int b) {
            return a + b;
        }

        private String hidden(String name) {
            return "hidden " + name;
        }
    }

    private static final MethodType INT_INT_TO_INT = MethodType.methodType(int.class, int.class, int.class);

    @Test
    void handlesCallAMethodAndReadAField() throws Throwable {
        Calculator calc = new Calculator();

        MethodHandle multiply = Handles.virtual(Calculator.class, "multiply", INT_INT_TO_INT);
        MethodHandle memory = Handles.getter(Calculator.class, "memory", int.class);

        assertThat((int) multiply.invoke(calc, 6, 7)).isEqualTo(42);
        assertThat((int) memory.invoke(calc)).isEqualTo(5);
    }

    @Test
    void invokeExactMatchesTheHandlesType() throws Throwable {
        Calculator calc = new Calculator();

        MethodHandle multiply = Handles.virtual(Calculator.class, "multiply", INT_INT_TO_INT);

        assertThat(multiply.type())
                .isEqualTo(MethodType.methodType(int.class, Calculator.class, int.class, int.class));
        int product = (int) multiply.invokeExact(calc, 6, 7);
        assertThat(product).isEqualTo(42);
    }

    @Test
    void aStaticMethodTakesNoReceiver() throws Throwable {
        MethodHandle add = Handles.staticMethod(Calculator.class, "add", INT_INT_TO_INT);

        int sum = (int) add.invokeExact(3, 4);
        assertThat(sum).isEqualTo(7);
    }

    @Test
    void privateMembersNeedAPrivateLookup() throws Throwable {
        Calculator calc = new Calculator();

        MethodHandle secret = Handles.getter(Calculator.class, "secret", String.class);
        MethodHandle hidden = Handles.virtual(Calculator.class, "hidden",
                MethodType.methodType(String.class, String.class));

        assertThat((String) secret.invoke(calc)).isEqualTo("s3");
        assertThat((String) hidden.invoke(calc, "ann")).isEqualTo("hidden ann");
    }
}
