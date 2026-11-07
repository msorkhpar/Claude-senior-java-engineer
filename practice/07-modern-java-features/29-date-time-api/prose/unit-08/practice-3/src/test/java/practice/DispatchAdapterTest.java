package practice;

import org.junit.jupiter.api.Test;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Depots in two zones with different offsets, so a solution that relies on the JVM's default
 * zone gets at least one of them wrong, whatever that default is. No test changes the default.
 */
class DispatchAdapterTest {

    private static final ZoneId PUNE = ZoneId.of("Asia/Kolkata");
    private static final ZoneId CAMPINAS = ZoneId.of("America/Sao_Paulo");

    /** A library that confirms the requested moment, written in the depot's zone. */
    private static final DispatchAdapter.Dispatcher CONFIRMS_AS_ASKED = (requested, depot) -> {
        GregorianCalendar confirmed = new GregorianCalendar(depot, Locale.ROOT);
        confirmed.setTimeInMillis(requested.getTimeInMillis());
        return confirmed;
    };

    private final DispatchAdapter adapter = new DispatchAdapter(CONFIRMS_AS_ASKED);

    @Test
    void aBookedPickupKeepsItsMoment() {
        ZonedDateTime pickup = ZonedDateTime.of(2025, 12, 2, 7, 45, 0, 0, PUNE);

        assertThat(adapter.book("Asia/Kolkata", pickup).toInstant()).isEqualTo(pickup.toInstant());
    }

    @Test
    void aConfiguredDepotGetsItsZone() {
        assertThat(adapter.depotZone("Asia/Kolkata").getID()).isEqualTo("Asia/Kolkata");
        assertThat(adapter.depotZone("America/Sao_Paulo").getID()).isEqualTo("America/Sao_Paulo");
    }

    @Test
    void aMisspelledDepotIsRefused() {
        assertThatThrownBy(() -> adapter.depotZone("Asia/Kolkatta")).isInstanceOf(DateTimeException.class);
    }

    @Test
    void theLibraryGetsThePickupOnTheDepotClock() {
        GregorianCalendar pune = adapter.toLibrary(ZonedDateTime.of(2025, 12, 2, 7, 45, 0, 0, PUNE));
        GregorianCalendar campinas = adapter.toLibrary(ZonedDateTime.of(2025, 12, 2, 7, 45, 0, 0, CAMPINAS));

        assertThat(pune.getTimeZone().getID()).isEqualTo("Asia/Kolkata");
        assertThat(pune.get(Calendar.HOUR_OF_DAY)).isEqualTo(7);
        assertThat(campinas.getTimeZone().getID()).isEqualTo("America/Sao_Paulo");
        assertThat(campinas.get(Calendar.HOUR_OF_DAY)).isEqualTo(7);
    }

    @Test
    void aReturnedCalendarIsReadOnItsOwnClock() {
        Instant confirmed = Instant.parse("2025-12-03T10:15:00Z");

        assertThat(adapter.fromLibrary(gregorian(PUNE, confirmed)).toString())
                .isEqualTo("2025-12-03T15:45+05:30[Asia/Kolkata]");
        assertThat(adapter.fromLibrary(gregorian(CAMPINAS, confirmed)).toString())
                .isEqualTo("2025-12-03T07:15-03:00[America/Sao_Paulo]");
    }

    @Test
    void aJapaneseCalendarFromTheLibraryConverts() {
        Calendar japanese = new Calendar.Builder()
                .setCalendarType("japanese")
                .setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"))
                .setInstant(Instant.parse("2025-12-03T10:15:00Z").toEpochMilli())
                .build();

        assertThat(adapter.fromLibrary(japanese).toString()).isEqualTo("2025-12-03T19:15+09:00[Asia/Tokyo]");
    }

    private static Calendar gregorian(ZoneId zone, Instant at) {
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone(zone), Locale.ROOT);
        calendar.setTimeInMillis(at.toEpochMilli());
        return calendar;
    }
}
