package practice;

import java.lang.reflect.Modifier;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LinkedStackTest {

    @Test
    void pushesAndPopsInLastInFirstOutOrder() {
        LinkedStack<Integer> stack = new LinkedStack<>();
        assertThat(stack.isEmpty()).isTrue();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        assertThat(stack.size()).isEqualTo(3);
        assertThat(stack.pop()).isEqualTo(3);
        assertThat(stack.peek()).isEqualTo(2);
        assertThat(stack.size()).isEqualTo(2);
        assertThat(stack.pop()).isEqualTo(2);
        assertThat(stack.pop()).isEqualTo(1);
        assertThat(stack.isEmpty()).isTrue();
    }

    @Test
    void anEmptyStackRefusesPopAndPeek() {
        LinkedStack<String> stack = new LinkedStack<>();
        assertThatThrownBy(stack::pop).isInstanceOf(NoSuchElementException.class);
        assertThatThrownBy(stack::peek).isInstanceOf(NoSuchElementException.class);
        stack.push("a");
        stack.pop();
        assertThatThrownBy(stack::pop).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void theNodeClassIsAPrivateStaticNestedClass() {
        LinkedStack<String> stack = new LinkedStack<>();
        stack.push("a");
        assertThat(stack.peek()).isEqualTo("a");
        Class<?>[] nested = LinkedStack.class.getDeclaredClasses();
        assertThat(nested).hasSize(1);
        int modifiers = nested[0].getModifiers();
        assertThat(Modifier.isPrivate(modifiers)).as("the node class is private").isTrue();
        assertThat(Modifier.isStatic(modifiers)).as("the node class is static").isTrue();
    }
}
