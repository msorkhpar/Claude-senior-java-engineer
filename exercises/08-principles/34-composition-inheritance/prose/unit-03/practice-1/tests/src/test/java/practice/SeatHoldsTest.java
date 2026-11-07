package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class SeatHoldsTest {

    /** Every case runs over every map the store can sit on. */
    private static final List<Supplier<SeatHolds>> BACKINGS =
            List.of(SeatHolds::plain, SeatHolds::sorted, SeatHolds::shared);
    private static final List<String> NAMES = List.of("plain", "sorted", "shared");

    @Test
    void holdsAndReleasesSeatsTheSameWayOverEveryMap() {
        for (int b = 0; b < BACKINGS.size(); b++) {
            SeatHolds store = BACKINGS.get(b).get();
            String on = NAMES.get(b);
            store.hold("R12-7", "Jane Doe");
            store.hold("R12-8", "John Roe");
            store.hold("R12-7", "Mia Poe");                     // a later hold replaces the earlier one
            assertThat(store.holder("R12-7")).as(on).contains("Mia Poe");
            assertThat(store.holder("R12-9")).as(on).isEmpty();
            assertThat(store.release("R12-8")).as(on).isTrue();
            assertThat(store.release("R12-8")).as(on).isFalse();
            assertThat(store.holder("R12-8")).as(on).isEmpty();
            assertThat(store.holds()).as(on).isEqualTo(Map.of("R12-7", "Mia Poe"));
        }
    }

    @Test
    void aHoldWithoutAGuestIsRefusedOverEveryMap() {
        for (int b = 0; b < BACKINGS.size(); b++) {
            SeatHolds store = BACKINGS.get(b).get();
            String on = NAMES.get(b);
            store.hold("B3", "Jane Doe");
            assertThatThrownBy(() -> store.hold("B3", null)).as(on).isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> store.hold("B4", null)).as(on).isInstanceOf(NullPointerException.class);
            assertThat(store.holder("B3")).as(on).contains("Jane Doe");
            assertThat(store.holds()).as(on).isEqualTo(Map.of("B3", "Jane Doe"));
        }
    }

    @Test
    void askingWhoHoldsNoSeatIsRefusedOverEveryMap() {
        for (int b = 0; b < BACKINGS.size(); b++) {
            SeatHolds store = BACKINGS.get(b).get();
            String on = NAMES.get(b);
            store.hold("G1", "John Roe");
            assertThatThrownBy(() -> store.holder(null)).as(on).isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    void aListOfHoldsStaysAsItWasWhileTheStoreChanges() {
        for (int b = 0; b < BACKINGS.size(); b++) {
            SeatHolds store = BACKINGS.get(b).get();
            String on = NAMES.get(b);
            store.hold("K1", "Ann Loe");
            store.hold("K2", "Bo Kay");
            Map<String, String> listed = store.holds();
            int walked = 0;
            for (String seat : listed.keySet()) {               // the store changes during the walk
                store.hold("K" + (10 + walked), "Cy Dee");
                store.release(seat);
                walked++;
            }
            assertThat(walked).as(on).isEqualTo(2);
            assertThat(listed).as(on).isEqualTo(Map.of("K1", "Ann Loe", "K2", "Bo Kay"));
        }
    }
}
