package practice;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Books courier pickups through a dispatch library written before Java 8. The library takes a
 * GregorianCalendar for the requested pickup and returns the confirmed pickup as a Calendar, in
 * the depot's zone. Depots are configured by zone ID.
 */
public class DispatchAdapter {

    /** The dispatch library. */
    public interface Dispatcher {
        Calendar confirm(GregorianCalendar requested, TimeZone depot);
    }

    private final Dispatcher dispatcher;

    public DispatchAdapter(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /** Books a pickup at a depot and returns the pickup the library confirmed. */
    public ZonedDateTime book(String depotZoneId, ZonedDateTime pickup) {
        TimeZone depot = depotZone(depotZoneId);
        return fromLibrary(dispatcher.confirm(toLibrary(pickup), depot));
    }

    /** The library's zone for a depot configured with {@code configuredId}. */
    TimeZone depotZone(String configuredId) {
        return TimeZone.getTimeZone(configuredId);
    }

    /** The calendar to hand the library for {@code pickup}. */
    GregorianCalendar toLibrary(ZonedDateTime pickup) {
        return GregorianCalendar.from(pickup);
    }

    /** A calendar the library returned, as the moment and zone it holds. */
    ZonedDateTime fromLibrary(Calendar returned) {
        return returned.toInstant().atZone(returned.getTimeZone().toZoneId());
    }
}
