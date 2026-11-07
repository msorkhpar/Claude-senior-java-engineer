package practice;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShipmentChecksTest {

    private static final ShipmentChecks.Shipment PALLET =
            new ShipmentChecks.Shipment("SH-5512", "Example Freight", List.of("p1", "p2", "p3"));
    private static final ShipmentChecks.Shipment LOOSE =
            new ShipmentChecks.Shipment("SH-5513", null, List.of("p9"));

    private final AtomicInteger descriptions = new AtomicInteger();
    private final AtomicInteger errors = new AtomicInteger();

    private final Function<ShipmentChecks.Shipment, String> describe = s -> {
        descriptions.incrementAndGet();
        return "shipment " + s.id() + " with " + s.parcels().size() + " parcels has no carrier";
    };
    private final Function<String, RuntimeException> notFound = id -> {
        errors.incrementAndGet();
        return new NoSuchElementException("no shipment " + id);
    };

    private final ShipmentChecks checks =
            new ShipmentChecks(Map.of(PALLET.id(), PALLET, LOOSE.id(), LOOSE), describe, notFound);

    @Test
    void aShipmentWithACarrierPassesAndOneWithoutFailsWithItsDescription() {
        assertThat(checks.carrierOf(PALLET)).isEqualTo("Example Freight");
        assertThatThrownBy(() -> checks.carrierOf(LOOSE))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("shipment SH-5513 with 1 parcels has no carrier");
    }

    @Test
    void aKnownIdIsFoundAndAnUnknownOneFailsWithTheBuiltError() {
        assertThat(checks.find("SH-5512")).isSameAs(PALLET);
        assertThatThrownBy(() -> checks.find("SH-0000"))
                .isExactlyInstanceOf(NoSuchElementException.class)
                .hasMessage("no shipment SH-0000");
    }

    @Test
    void shipmentsThatPassAreNeverDescribed() {
        for (int i = 0; i < 500; i++) checks.carrierOf(PALLET);

        assertThat(descriptions).as("descriptions built for passing shipments").hasValue(0);
    }

    @Test
    void lookupsThatSucceedBuildNoError() {
        for (int i = 0; i < 500; i++) checks.find(i % 2 == 0 ? "SH-5512" : "SH-5513");

        assertThat(errors).as("errors built for ids that exist").hasValue(0);
    }
}
