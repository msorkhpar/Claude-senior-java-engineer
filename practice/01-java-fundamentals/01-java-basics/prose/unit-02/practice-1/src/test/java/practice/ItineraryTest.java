package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ItineraryTest {

    /** A booked trip, built afresh for each test: Lisbon to Oslo through Amsterdam, and back. */
    private static Itinerary booked() {
        List<Itinerary.Leg> legs = new ArrayList<>();
        legs.add(new Itinerary.Leg("TP662", "14C", new ArrayList<>(List.of("ski bag"))));
        legs.add(new Itinerary.Leg("KL1143", "22A", new ArrayList<>()));
        legs.add(new Itinerary.Leg("SK4417", "3F", new ArrayList<>(List.of("cabin case", "golf clubs"))));
        return new Itinerary("QX7RTB", legs);
    }

    @Test
    void anAmendmentStartsAsTheBookedTrip() {
        Itinerary trip = booked();

        Itinerary amendment = trip.amendment();

        assertThat(amendment).isNotSameAs(trip);
        assertThat(amendment.reference()).isEqualTo("QX7RTB");
        assertThat(amendment.toString()).isEqualTo(
                "QX7RTB [TP662 14C [ski bag], KL1143 22A [], SK4417 3F [cabin case, golf clubs]]");
    }

    @Test
    void droppingALegFromTheAmendmentKeepsItOnTheBooking() {
        Itinerary trip = booked();
        Itinerary amendment = trip.amendment();

        amendment.legs().remove(1);

        assertThat(trip.legs()).extracting(Itinerary.Leg::flight).containsExactly("TP662", "KL1143", "SK4417");
    }

    @Test
    void aSeatChangedOnTheAmendmentLeavesTheBookedSeat() {
        Itinerary trip = booked();
        Itinerary amendment = trip.amendment();

        amendment.legs().get(2).changeSeat("1A");

        assertThat(trip.legs().get(2).seat()).isEqualTo("3F");
        assertThat(amendment.legs().get(2).seat()).isEqualTo("1A");
    }

    @Test
    void aBagAddedOnTheAmendmentIsNotOnTheBooking() {
        Itinerary trip = booked();
        Itinerary amendment = trip.amendment();

        amendment.legs().get(0).bags().add("bicycle");

        assertThat(trip.legs().get(0).bags()).containsExactly("ski bag");
        assertThat(amendment.legs().get(0).bags()).containsExactly("ski bag", "bicycle");
    }

    @Test
    void theAgentCanAddALegToTheAmendment() {
        Itinerary trip = booked();
        Itinerary amendment = trip.amendment();

        assertThatCode(() -> amendment.legs().add(new Itinerary.Leg("SK4420", "5D", new ArrayList<>())))
                .doesNotThrowAnyException();
        assertThat(amendment.legs()).hasSize(4);
        assertThat(trip.legs()).hasSize(3);
    }
}
