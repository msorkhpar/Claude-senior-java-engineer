package practice;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArrayStackTest {

    /** Every Object[] field of the stack, read by reflection. */
    private static List<Object[]> arraysInside(ArrayStack<?> stack) throws IllegalAccessException {
        List<Object[]> arrays = new ArrayList<>();
        for (Field f : stack.getClass().getDeclaredFields()) {
            if (f.getType() == Object[].class) {
                f.setAccessible(true);
                arrays.add((Object[]) f.get(stack));
            }
        }
        return arrays;
    }

    @Test
    void popsInLastInFirstOutOrder() {
        ArrayStack<String> stack = new ArrayStack<>(4);
        stack.push("a");
        stack.push("b");
        stack.push("c");

        assertThat(stack.pop()).isEqualTo("c");
        assertThat(stack.pop()).isEqualTo("b");
        assertThat(stack.size()).isEqualTo(1);
        assertThat(stack.peek()).isEqualTo("a");
        assertThat(stack.size()).isEqualTo(1);
    }

    @Test
    void popClearsTheSlotItFrees() throws Exception {
        ArrayStack<Object> stack = new ArrayStack<>(4);
        Object a = new Object();
        Object b = new Object();
        Object c = new Object();
        stack.push(a);
        stack.push(b);
        stack.push(c);
        stack.pop();
        stack.pop();

        List<Object[]> arrays = arraysInside(stack);
        assertThat(arrays).as("the stack keeps its elements in an Object[] field").isNotEmpty();
        for (Object[] array : arrays) {
            for (Object slot : array) {
                assertThat(slot).as("a popped element is still referenced").isNotSameAs(b).isNotSameAs(c);
            }
        }
        assertThat(stack.peek()).isSameAs(a);
    }

    @Test
    void anEmptyStackRefusesPopAndPeek() {
        ArrayStack<String> stack = new ArrayStack<>(2);
        assertThatThrownBy(stack::pop).isInstanceOf(EmptyStackException.class);
        assertThatThrownBy(stack::peek).isInstanceOf(EmptyStackException.class);

        stack.push("x");
        stack.pop();
        assertThatThrownBy(stack::pop).isInstanceOf(EmptyStackException.class);
    }

    @Test
    void growsPastItsFirstCapacity() {
        ArrayStack<Integer> stack = new ArrayStack<>(2);
        for (int i = 0; i < 100; i++) {
            stack.push(1000 + i);
        }

        assertThat(stack.size()).isEqualTo(100);
        for (int i = 99; i >= 0; i--) {
            assertThat(stack.pop()).isEqualTo(1000 + i);
        }
    }
}
