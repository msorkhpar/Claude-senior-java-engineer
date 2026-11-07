package practice;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import practice.LabelBooking.AddressRejected;
import practice.LabelBooking.CarrierException;

class LabelBookingTest {

    /** Answers each booking with the next scripted tracking number or exception. */
    static final class ScriptedCarrier implements LabelBooking.Carrier {
        private final Deque<Object> answers = new ArrayDeque<>();
        final List<String> addresses = new ArrayList<>();

        ScriptedCarrier(Object... answers) {
            this.answers.addAll(List.of(answers));
        }

        @Override
        public String book(String parcel, String address) {
            addresses.add(address);
            Object next = answers.poll();
            if (next == null) {
                throw new AssertionError("the carrier was called more often than scripted");
            }
            if (next instanceof RuntimeException e) {
                throw e;
            }
            return (String) next;
        }
    }

    /** Runs ship() and returns what it returned, or the text of what it threw. */
    private static String outcome(LabelBooking booking, String parcel, String address) {
        AtomicReference<String> result = new AtomicReference<>();
        try {
            result.set(booking.ship(parcel, address));
        } catch (RuntimeException e) {
            result.set("threw " + e);
        }
        return result.get();
    }

    @Test
    void aRejectedAddressIsBookedAgainInItsNormalizedForm() {
        ScriptedCarrier carrier = new ScriptedCarrier(
                new AddressRejected("street not found"), "1Z-48820-QX");

        String result = outcome(new LabelBooking(carrier), "PCL-5521", "  17   Mill   Row,  york ");

        assertThat(result).isEqualTo("booked: 1Z-48820-QX");
        assertThat(carrier.addresses).containsExactly("  17   Mill   Row,  york ", "17 MILL ROW, YORK");
    }

    @Test
    void aSecondRejectionGoesToReview() {
        ScriptedCarrier carrier = new ScriptedCarrier(
                new AddressRejected("street not found"),
                new AddressRejected("postcode YO9 3QQ does not exist"));

        String result = outcome(new LabelBooking(carrier), "PCL-8807", "3 Kiln  Yard, YO9 3QQ");

        assertThat(result).isEqualTo("review: postcode YO9 3QQ does not exist");
        assertThat(carrier.addresses).hasSize(2);
    }

    @Test
    void aCarrierOutageDuringTheSecondAttemptIsRetriedLater() {
        ScriptedCarrier carrier = new ScriptedCarrier(
                new AddressRejected("street not found"),
                new CarrierException("502 Bad Gateway from /labels"));

        String result = outcome(new LabelBooking(carrier), "PCL-1290", "88 Wharf  St, Hull");

        assertThat(result).isEqualTo("retry later: 502 Bad Gateway from /labels");
        assertThat(carrier.addresses).hasSize(2);
    }
}
