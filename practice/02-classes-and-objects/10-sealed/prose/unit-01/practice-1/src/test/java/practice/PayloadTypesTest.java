package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;

class PayloadTypesTest {

    sealed interface Shipment permits Dispatched, Delivered, Returned {}
    record Dispatched(String parcel, String depot) implements Shipment {}
    record Delivered(String parcel, String signedBy) implements Shipment {}
    record Returned(String parcel, String reason) implements Shipment {}

    sealed interface Tracking permits InTransit, Incident {}
    record InTransit(String parcel, String hub) implements Tracking {}
    sealed interface Incident extends Tracking permits Lost, Damaged {}
    record Lost(String parcel) implements Incident {}
    sealed interface Damaged extends Incident permits Crushed, Soaked {}
    record Crushed(String parcel, String photo) implements Damaged {}
    record Soaked(String parcel, String photo) implements Damaged {}

    interface Notice {}
    record Delay(String parcel, int hours) implements Notice {}

    @Test
    void mapsEveryPayloadOfAFlatHierarchyToItsRecord() {
        assertThat(PayloadTypes.of(Shipment.class)).containsExactlyInAnyOrderEntriesOf(Map.of(
                "dispatched", Dispatched.class,
                "delivered", Delivered.class,
                "returned", Returned.class));
    }

    @Test
    void reachesTheRecordsUnderASealedSubtypeAtAnyDepth() {
        assertThat(PayloadTypes.of(Tracking.class)).containsExactlyInAnyOrderEntriesOf(Map.of(
                "intransit", InTransit.class,
                "lost", Lost.class,
                "crushed", Crushed.class,
                "soaked", Soaked.class));
    }

    @Test
    void refusesARootWhoseImplementationsNobodyCanList() {
        assertThatThrownBy(() -> PayloadTypes.of(Notice.class))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
