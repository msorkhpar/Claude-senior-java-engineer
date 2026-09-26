package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TypeKindsTest {

    record Point(int x, int y) {
    }

    @Test
    void classifiesCommonTypes() {
        assertThat(TypeKinds.kind(String.class)).isEqualTo("class");
        assertThat(TypeKinds.kind(int[].class)).isEqualTo("array");
        assertThat(TypeKinds.kind(Runnable.class)).isEqualTo("interface");
        assertThat(TypeKinds.kind(Thread.State.class)).isEqualTo("enum");
        assertThat(TypeKinds.kind(Point.class)).isEqualTo("record");
        assertThat(TypeKinds.elementType(int[].class)).isSameAs(int.class);
        assertThat(TypeKinds.elementType(String.class)).isNull();
    }

    @Test
    void anAnnotationIsNotAPlainInterface() {
        assertThat(TypeKinds.kind(Override.class)).isEqualTo("annotation");
        assertThat(TypeKinds.kind(FunctionalInterface.class)).isEqualTo("annotation");
    }

    @Test
    void primitivesAreFoundByIsPrimitive() {
        assertThat(TypeKinds.kind(int.class)).isEqualTo("primitive");
        assertThat(TypeKinds.kind(void.class)).isEqualTo("primitive");
        assertThat(TypeKinds.kind(Object.class)).isEqualTo("class");
    }

    @Test
    void nestedArraysGiveTheInnermostElement() {
        assertThat(TypeKinds.elementType(int[][].class)).isSameAs(int.class);
        assertThat(TypeKinds.elementType(String[][][].class)).isSameAs(String.class);
    }
}
