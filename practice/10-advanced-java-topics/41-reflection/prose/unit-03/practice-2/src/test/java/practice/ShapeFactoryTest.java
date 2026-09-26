package practice;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class ShapeFactoryTest {

    static class Circle implements ShapeFactory.Shape {
        @Override
        public double area() {
            return Math.PI * 4;
        }
    }

    static class Square implements ShapeFactory.Shape {
        @Override
        public double area() {
            return 16;
        }
    }

    static class Sized implements ShapeFactory.Shape {
        private final double side;

        Sized(double side) {
            this.side = side;
        }

        @Override
        public double area() {
            return side * side;
        }
    }

    @Test
    void createsRegisteredShapes() {
        ShapeFactory factory = new ShapeFactory();
        factory.register("circle", Circle.class);
        factory.register("square", Square.class);

        ShapeFactory.Shape circle = factory.create("circle");
        assertThat(circle).isInstanceOf(Circle.class);
        assertThat(circle.area()).isCloseTo(Math.PI * 4, within(1e-9));
        assertThat(factory.create("Square").area()).isCloseTo(16.0, within(1e-9));
        assertThat(factory.create("circle")).isNotSameAs(circle);
        assertThatThrownBy(() -> factory.create("hexagon")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void namesIgnoreCaseInAnyLocale() {
        Locale saved = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            ShapeFactory factory = new ShapeFactory();
            factory.register("CIRCLE", Circle.class);

            assertThat(factory.create("circle")).isInstanceOf(Circle.class);
        } finally {
            Locale.setDefault(saved);
        }
    }

    @Test
    void aClassWithoutANoArgConstructorIsRefusedAtRegistration() {
        ShapeFactory factory = new ShapeFactory();

        assertThatThrownBy(() -> factory.register("sized", Sized.class))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
