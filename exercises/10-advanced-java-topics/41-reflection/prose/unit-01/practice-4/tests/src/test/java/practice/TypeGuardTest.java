package practice;

import java.util.AbstractList;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TypeGuardTest {

    abstract static class Base {
    }

    @Test
    void checksExactTypesAndModifiers() {
        assertThat(TypeGuard.accepts(String.class, new String("x"))).isTrue();
        assertThat(TypeGuard.accepts(Integer.class, new String("x"))).isFalse();
        assertThat(TypeGuard.isAbstractClass(AbstractList.class)).isTrue();
        assertThat(TypeGuard.isAbstractClass(String.class)).isFalse();
        assertThat(TypeGuard.modifiers(String.class)).isEqualTo("public final");
        assertThat(TypeGuard.modifiers(Base.class)).isEqualTo("abstract static");
    }

    @Test
    void subtypesAreAccepted() {
        assertThat(TypeGuard.accepts(CharSequence.class, new StringBuilder("x"))).isTrue();
        assertThat(TypeGuard.accepts(Number.class, Long.valueOf(5000L))).isTrue();
    }

    @Test
    void nullIsNeverAccepted() {
        assertThat(TypeGuard.accepts(String.class, null)).isFalse();
        assertThat(TypeGuard.accepts(Object.class, null)).isFalse();
    }

    @Test
    void anInterfaceIsNotAnAbstractClass() {
        assertThat(TypeGuard.isAbstractClass(Runnable.class)).isFalse();
        assertThat(TypeGuard.isAbstractClass(Override.class)).isFalse();
    }
}
