package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class ShipmentTest {

    private static List<String> labels(String... names) {
        return new ArrayList<>(Arrays.asList(names));
    }

    @Test
    void aCompleteShipmentIsAcceptedAsSent() {
        Shipment s = new Shipment("NL-77310-K", "Rotterdam", 3, labels("crate-a", "crate-b", "crate-c"));

        assertThat(s.trackingId()).isEqualTo("NL-77310-K");
        assertThat(s.parcels()).isEqualTo(3);
        assertThat(s.labels()).containsExactly("crate-a", "crate-b", "crate-c");
        assertThat(s.routingCode()).isEqualTo("ROTTERDAM/NL-77310-K/3");
    }

    @Test
    void theSmallestAndTheLargestShipmentsAreAccepted() {
        Shipment one = new Shipment("PL-00412-B", "Gdansk", 1, labels("drum"));
        Shipment twenty = new Shipment("PL-00413-B", "Gdansk", 20, labels("pallet"));

        assertThat(one.parcels()).isEqualTo(1);
        assertThat(twenty.parcels()).isEqualTo(20);
    }

    @Test
    void aShipmentWithNoDestinationIsRefusedWhenTheFeedCreatesIt() {
        assertThatThrownBy(() -> new Shipment("BE-55120-Q", null, 4, labels("box-1")))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void aBlankTrackingIdIsRefused() {
        assertThatThrownBy(() -> new Shipment("   ", "Antwerp", 2, labels("box-1")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Shipment("", "Antwerp", 2, labels("box-1")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aShipmentOfNoParcelsOrTooManyIsRefused() {
        assertThatThrownBy(() -> new Shipment("DE-90031-T", "Hamburg", 0, labels()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Shipment("DE-90032-T", "Hamburg", 21, labels("x")))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Shipment("DE-90033-T", "Hamburg", -2, labels("x")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void theFeedChangingItsListAfterwardsDoesNotChangeTheShipment() {
        List<String> fromFeed = labels("tube-1", "tube-2");
        Shipment s = new Shipment("FR-31877-M", "Lyon", 2, fromFeed);

        fromFeed.set(0, "tube-9");
        fromFeed.add(null);

        assertThat(s.labels()).containsExactly("tube-1", "tube-2");
    }

    @Test
    void aMissingLabelIsRefusedWhenTheFeedCreatesTheShipment() {
        assertThatThrownBy(() -> new Shipment("IT-60218-R", "Milan", 2, labels("sack-1", null)))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Shipment("IT-60219-R", "Milan", 2, null))
                .isInstanceOf(NullPointerException.class);
    }
}
