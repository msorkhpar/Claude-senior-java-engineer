package com.github.msorkhpar.claudejavatutor.oo;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

class AbstractionTest {

    @Test
    void testCarBehavior() {
        Car car = new Car("Toyota");
        
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        car.start();
        assertThat(outContent.toString().trim()).isEqualTo("Toyota car is starting.");
        outContent.reset();

        car.accelerate();
        assertThat(outContent.toString().trim()).isEqualTo("Toyota car is accelerating.");
        outContent.reset();

        car.brake();
        assertThat(outContent.toString().trim()).isEqualTo("Toyota car is braking.");
        outContent.reset();

        car.stop();
        assertThat(outContent.toString().trim()).isEqualTo("Toyota vehicle is stopping.");
        outContent.reset();

        car.honk();
        assertThat(outContent.toString().trim()).isEqualTo("Honk honk!");

        System.setOut(System.out);
    }

    @Test
    void testAbstractionWithMockito() {
        Vehicle vehicleMock = mock(Vehicle.class);
        Drivable drivableMock = mock(Drivable.class);

        vehicleMock.stop();
        verify(vehicleMock).stop();

        drivableMock.accelerate();
        verify(drivableMock).accelerate();
    }

    @Test
    void testPolymorphism() {
        Vehicle car = new Car("Honda");
        Drivable drivableCar = new Car("Ford");

        assertThat(car).isInstanceOf(Vehicle.class);
        assertThat(car).isInstanceOf(Drivable.class);
        assertThat(drivableCar).isInstanceOf(Drivable.class);
        assertThat(drivableCar).isInstanceOf(Vehicle.class);
    }
}