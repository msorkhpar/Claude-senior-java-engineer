package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TypeRegistryTest {

    // A closed hierarchy under an interface.
    sealed interface Shipment permits Parcel, Pallet, Letter {}
    record Parcel(int grams) implements Shipment {}
    record Pallet(int kilograms) implements Shipment {}
    static final class Letter implements Shipment {}

    // A closed hierarchy under an abstract class.
    abstract static sealed class Alert permits Page, Email {}
    static final class Page extends Alert {}
    static final class Email extends Alert {}

    // A sealed class that has instances of its own.
    sealed interface Ticket permits Seat, Pass {}
    static sealed class Seat implements Ticket permits WindowSeat {}
    static final class WindowSeat extends Seat {}
    record Pass(String holder) implements Ticket {}

    // A second level that is sealed and abstract.
    sealed interface Reading permits Meter, Estimate {}
    abstract static sealed class Meter implements Reading permits GasMeter, WaterMeter {}
    static final class GasMeter extends Meter {}
    static final class WaterMeter extends Meter {}
    record Estimate(double cubicMetres) implements Reading {}

    // A class branch anyone may extend.
    sealed interface Payout permits BankPayout, PartnerPayout {}
    record BankPayout(String iban) implements Payout {}
    static non-sealed class PartnerPayout implements Payout {}

    // An interface branch anyone may implement.
    sealed interface Notice permits Banner, CustomNotice {}
    record Banner(String text) implements Notice {}
    non-sealed interface CustomNotice extends Notice {}

    @Test
    void registersEveryKindOfAClosedHierarchy() {
        TypeRegistry registry = TypeRegistry.of(Shipment.class);

        assertThat(registry.registered()).containsExactlyInAnyOrder(Parcel.class, Pallet.class, Letter.class);
        assertThat(registry.typeId(new Pallet(120))).isEqualTo("Pallet");
    }

    @Test
    void registersTheSubclassesOfAnAbstractRoot() {
        TypeRegistry registry = TypeRegistry.of(Alert.class);

        assertThat(registry.registered()).containsExactlyInAnyOrder(Page.class, Email.class);
    }

    @Test
    void refusesAClassBranchAnyoneCanExtend() {
        assertThatThrownBy(() -> TypeRegistry.of(Payout.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PartnerPayout");
    }

    @Test
    void registersASealedClassThatHasInstancesOfItsOwn() {
        TypeRegistry registry = TypeRegistry.of(Ticket.class);

        assertThat(registry.registered()).containsExactlyInAnyOrder(Seat.class, WindowSeat.class, Pass.class);
        assertThat(registry.typeId(new Seat())).isEqualTo("Seat");
    }

    @Test
    void acceptsASealedBranchBelowTheRoot() {
        TypeRegistry registry = TypeRegistry.of(Reading.class);

        assertThat(registry.registered())
                .containsExactlyInAnyOrder(GasMeter.class, WaterMeter.class, Estimate.class);
    }

    @Test
    void refusesAnInterfaceBranchAnyoneCanImplement() {
        assertThatThrownBy(() -> TypeRegistry.of(Notice.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CustomNotice");
    }
}
