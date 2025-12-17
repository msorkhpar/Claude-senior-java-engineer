package com.github.msorkhpar.claudejavatutor.trycatch;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class SealedClassesTest {

    @Test
    void testShapeHierarchy() {
        Shape circle = new Circle(5);
        Shape square = new Square(4);
        Shape triangle = new Triangle(3, 4);

        assertThat(circle).isInstanceOf(Circle.class);
        assertThat(square).isInstanceOf(Square.class);
        assertThat(triangle).isInstanceOf(Triangle.class);

        assertThat(circle.getName()).isEqualTo("Circle");
        assertThat(square.getName()).isEqualTo("Square");
        assertThat(triangle.getName()).isEqualTo("Triangle");

        assertThat(circle.area()).isCloseTo(78.54, within(0.01));
        assertThat(square.area()).isEqualTo(16.0);
        assertThat(triangle.area()).isEqualTo(6.0);
    }

    @Test
    void testVehicleInterface() {
        Vehicle car = new Car("Tesla");
        Vehicle motorcycle = new Motorcycle("Harley-Davidson");
        Vehicle truck = new Truck(10000);

        assertThat(car).isInstanceOf(Car.class);
        assertThat(motorcycle).isInstanceOf(Motorcycle.class);
        assertThat(truck).isInstanceOf(Truck.class);

        assertThat(car.getType()).isEqualTo("Car");
        assertThat(motorcycle.getType()).isEqualTo("Motorcycle");
        assertThat(truck.getType()).isEqualTo("Truck");

        assertThat(car.getWheels()).isEqualTo(4);
        assertThat(motorcycle.getWheels()).isEqualTo(2);
        assertThat(truck.getWheels()).isEqualTo(6);
    }

    @Test
    void testExhaustivePatternMatching() {
        Shape shape = new Circle(3);
        String result = switch (shape) {
            case Circle c -> "Circle with area: " + c.area();
            case Square s -> "Square with area: " + s.area();
            case Triangle t -> "Triangle with area: " + t.area();
        };
        assertThat(result).startsWith("Circle with area:");
    }
}