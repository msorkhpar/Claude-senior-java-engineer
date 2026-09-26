package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InheritanceTest {

    abstract static sealed class Shape permits Circle, Triangle {
    }

    static final class Circle extends Shape {
    }

    static non-sealed class Triangle extends Shape {
    }

    /** No permits clause: the subclasses in this file are the permitted ones. */
    abstract static sealed class Implicit {
    }

    static final class Only extends Implicit {
    }

    static class Plain {
    }

    static class Square {
    }

    @Test
    void aPermittedSubclassIsAccepted() {
        assertThat(Inheritance.verdict(Shape.class, Circle.class)).isNull();
        assertThat(Inheritance.verdict(Shape.class, Triangle.class)).isNull();
        assertThat(Inheritance.verdict(Implicit.class, Only.class)).isNull();
        assertThat(Inheritance.verdict(Plain.class, Square.class)).isNull();
    }

    @Test
    void anUnlistedClassIsRejected() {
        assertThat(Inheritance.verdict(Shape.class, Square.class))
                .isEqualTo("class is not allowed to extend sealed class: Shape");
        assertThat(Inheritance.verdict(Implicit.class, Square.class))
                .isEqualTo("class is not allowed to extend sealed class: Implicit");
    }

    @Test
    void aFinalClassAcceptsNoSubclass() {
        assertThat(Inheritance.verdict(Circle.class, Square.class))
                .isEqualTo("cannot inherit from final Circle");
    }

    @Test
    void aNonSealedClassAcceptsAnySubclass() {
        assertThat(Inheritance.verdict(Triangle.class, Square.class)).isNull();
    }
}
