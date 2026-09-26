package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SealedTreeTest {

    sealed interface Signal permits Red, Amber, Green {
    }

    record Red() implements Signal {
    }

    record Amber() implements Signal {
    }

    record Green() implements Signal {
    }

    abstract static sealed class Vehicle permits Car, Truck, Motorcycle {
    }

    static final class Car extends Vehicle {
    }

    abstract static sealed class Truck extends Vehicle permits PickupTruck, SemiTruck {
    }

    static final class PickupTruck extends Truck {
    }

    static final class SemiTruck extends Truck {
    }

    static non-sealed class Motorcycle extends Vehicle {
    }

    @Test
    void listsThePermittedKindsInOrder() {
        assertThat(SealedTree.leaves(Signal.class)).containsExactly("Red", "Amber", "Green");
    }

    @Test
    void aSealedSubclassIsExpanded() {
        assertThat(SealedTree.leaves(Vehicle.class))
                .containsExactly("Car", "PickupTruck", "SemiTruck", "Motorcycle+");
        assertThat(SealedTree.leaves(Truck.class)).containsExactly("PickupTruck", "SemiTruck");
    }

    @Test
    void aNonSealedSubclassIsMarkedOpen() {
        assertThat(SealedTree.leaves(Vehicle.class)).endsWith("Motorcycle+");
    }

    @Test
    void aTypeThatIsNotSealedIsRefused() {
        assertThatThrownBy(() -> SealedTree.leaves(String.class))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> SealedTree.leaves(Motorcycle.class))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
