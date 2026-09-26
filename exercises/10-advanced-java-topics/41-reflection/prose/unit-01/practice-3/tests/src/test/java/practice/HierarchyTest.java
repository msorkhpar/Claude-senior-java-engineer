package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HierarchyTest {

    interface Drawable {
    }

    interface Resizable {
    }

    interface Scalable extends Resizable {
    }

    static class Plain implements Drawable {
    }

    abstract static class Shape implements Drawable {
    }

    static class Square extends Shape {
    }

    static class Knob implements Scalable {
    }

    static class Circle extends Shape implements Scalable {
    }

    @Test
    void walksASimpleClass() {
        assertThat(Hierarchy.superclasses(Plain.class)).containsExactly(Plain.class, Object.class);
        assertThat(Hierarchy.allInterfaces(Plain.class)).containsExactlyInAnyOrder(Drawable.class);
        assertThat(Hierarchy.superclasses(Circle.class)).containsExactly(Circle.class, Shape.class, Object.class);
    }

    @Test
    void interfacesOfASuperclassAreIncluded() {
        assertThat(Hierarchy.allInterfaces(Square.class)).containsExactlyInAnyOrder(Drawable.class);
    }

    @Test
    void superInterfacesAreIncluded() {
        assertThat(Hierarchy.allInterfaces(Knob.class))
                .containsExactlyInAnyOrder(Scalable.class, Resizable.class);
        assertThat(Hierarchy.allInterfaces(Circle.class))
                .containsExactlyInAnyOrder(Scalable.class, Resizable.class, Drawable.class);
    }

    @Test
    void anInterfaceOrPrimitiveHasNoSuperclass() {
        assertThat(Hierarchy.superclasses(Scalable.class)).containsExactly(Scalable.class);
        assertThat(Hierarchy.superclasses(int.class)).containsExactly(int.class);
        assertThat(Hierarchy.superclasses(Object.class)).containsExactly(Object.class);
    }
}
