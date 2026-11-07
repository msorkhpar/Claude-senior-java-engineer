package practice;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingTest {

    private static final LocalDateTime MON_0830 = LocalDateTime.of(2027, 3, 8, 8, 30);
    private static final LocalDateTime MON_0915 = LocalDateTime.of(2027, 3, 8, 9, 15);
    private static final LocalDateTime MON_1300 = LocalDateTime.of(2027, 3, 8, 13, 0);
    private static final LocalDateTime MON_1445 = LocalDateTime.of(2027, 3, 8, 14, 45);

    @Test
    void aBookingFilledInFieldByFieldIsCreated() {
        Booking b = Booking.builder()
                .room("Fjord").organiser("Jane Doe").start(MON_0830).end(MON_0915)
                .build();

        assertThat(b.room()).isEqualTo("Fjord");
        assertThat(b.organiser()).isEqualTo("Jane Doe");
        assertThat(b.start()).isEqualTo(MON_0830);
        assertThat(b.end()).isEqualTo(MON_0915);
    }

    @Test
    void theFieldsMayBeFilledInAnyOrder() {
        Booking b = Booking.builder()
                .end(MON_1445).organiser("John Roe").start(MON_1300).room("Atrium")
                .build();

        assertThat(b.start()).isEqualTo(MON_1300);
        assertThat(b.end()).isEqualTo(MON_1445);
        assertThat(b.room()).isEqualTo("Atrium");
    }

    @Test
    void aBookingWhoseEndWasFilledInFirstMustStillEndAfterItStarts() {
        Booking.Builder form = Booking.builder().room("Fjord").organiser("Jane Doe")
                .end(MON_0830).start(MON_0915);

        assertThatThrownBy(form::build).isInstanceOf(Booking.InvalidBooking.class);
    }

    @Test
    void aBookingThatEndsWhenItStartsIsRefused() {
        Booking.Builder form = Booking.builder().room("Atrium").organiser("John Roe")
                .start(MON_1300).end(MON_1300);

        assertThatThrownBy(form::build).isInstanceOf(Booking.InvalidBooking.class);
    }

    @Test
    void aMissingOrganiserIsReportedToTheForm() {
        Booking.Builder form = Booking.builder().room("Atrium").start(MON_1300).end(MON_1445);

        assertThatThrownBy(form::build).isInstanceOf(Booking.InvalidBooking.class);
    }

    @Test
    void aBookingMovedToTheAfternoonIsCreatedThere() {
        Booking.Builder form = Booking.builder().room("Fjord").organiser("Jane Doe")
                .start(MON_0830).end(MON_0915);

        Booking moved = form.start(MON_1300).end(MON_1445).build();

        assertThat(moved.start()).isEqualTo(MON_1300);
        assertThat(moved.end()).isEqualTo(MON_1445);
    }
}
