package practice;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.Test;

class ShipmentTest {

    private static Date at(String instant) {
        return Date.from(Instant.parse(instant));
    }

    @Test
    void reportsTheDispatchMomentItWasGiven() {
        Shipment s = new Shipment("TRK-4471", at("2025-09-02T14:05:00Z"));

        assertThat(s.dispatchedAt().toInstant()).isEqualTo(Instant.parse("2025-09-02T14:05:00Z"));
        assertThat(s.dispatchedBefore(at("2025-09-02T15:00:00Z"))).isTrue();
        assertThat(s.dispatchedBefore(at("2025-09-02T14:00:00Z"))).isFalse();
    }

    @Test
    void aCallerReusingItsDateForTheNextParcelLeavesThisOneAlone() {
        Date scanned = at("2025-09-02T14:05:00Z");
        Shipment first = new Shipment("TRK-4471", scanned);

        scanned.setTime(Instant.parse("2025-09-03T07:30:00Z").toEpochMilli()); // next parcel's scan
        Shipment second = new Shipment("TRK-4472", scanned);

        assertThat(first.dispatchedAt().toInstant()).isEqualTo(Instant.parse("2025-09-02T14:05:00Z"));
        assertThat(second.dispatchedAt().toInstant()).isEqualTo(Instant.parse("2025-09-03T07:30:00Z"));
    }

    @Test
    void shiftingTheReturnedDateLeavesTheShipmentAlone() {
        Shipment s = new Shipment("TRK-4471", at("2025-09-02T14:05:00Z"));

        Date shown = s.dispatchedAt();
        shown.setTime(shown.getTime() + 2 * 60 * 60 * 1000);            // a report shifts it to local time

        assertThat(s.dispatchedAt().toInstant()).isEqualTo(Instant.parse("2025-09-02T14:05:00Z"));
        assertThat(s.dispatchedBefore(at("2025-09-02T15:00:00Z"))).isTrue();
    }
}
