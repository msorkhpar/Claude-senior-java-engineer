package practice;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class ParcelTimelineTest {

    @Test
    void ordersCourierEventsAndScansFromADepotOnUtc() {
        ParcelTimeline parcel = new ParcelTimeline(ZoneOffset.UTC);
        parcel.addCourier("out for delivery", "2026-11-21T07:10:00Z");
        parcel.addDepotScan("sorted", "2026-11-20T23:55:00");
        parcel.addCourier("delivered", "2026-11-21T09:42:30Z");
        parcel.addDepotScan("arrived at depot", "2026-11-20T21:03:00");

        assertThat(parcel.events()).containsExactly(
                new ParcelTimeline.Event("arrived at depot", Instant.parse("2026-11-20T21:03:00Z")),
                new ParcelTimeline.Event("sorted", Instant.parse("2026-11-20T23:55:00Z")),
                new ParcelTimeline.Event("out for delivery", Instant.parse("2026-11-21T07:10:00Z")),
                new ParcelTimeline.Event("delivered", Instant.parse("2026-11-21T09:42:30Z")));
    }

    @Test
    void placesALineHaulEventAtTheMomentItsSentAtNames() {
        ParcelTimeline parcel = new ParcelTimeline(ZoneOffset.UTC);
        parcel.addLineHaul("picked up", 1_795_200_000L);
        parcel.addCourier("delivered", "2026-11-21T09:42:30Z");

        assertThat(parcel.events()).containsExactly(
                new ParcelTimeline.Event("picked up", Instant.parse("2026-11-20T18:40:00Z")),
                new ParcelTimeline.Event("delivered", Instant.parse("2026-11-21T09:42:30Z")));
    }

    @Test
    void placesAScanFromADepotWestOfUtcAtTheMomentOfTheScan() {
        ParcelTimeline parcel = new ParcelTimeline(ZoneId.of("America/Chicago"));
        parcel.addDepotScan("arrived at depot", "2026-11-20T16:45:00");
        parcel.addCourier("handed to courier", "2026-11-20T22:00:00Z");

        assertThat(parcel.events()).containsExactly(
                new ParcelTimeline.Event("handed to courier", Instant.parse("2026-11-20T22:00:00Z")),
                new ParcelTimeline.Event("arrived at depot", Instant.parse("2026-11-20T22:45:00Z")));
    }
}
