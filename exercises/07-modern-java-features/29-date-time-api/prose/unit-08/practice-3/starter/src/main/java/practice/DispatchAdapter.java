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
        // TODO: Return the zone of the depot. A depot whose configured ID names no zone
        //       must not be booked.
        throw new IllegalStateException("TODO: depotZone");
    }

    /** The calendar to hand the library for {@code pickup}. */
    GregorianCalendar toLibrary(ZonedDateTime pickup) {
        // TODO: Return a calendar for the pickup that the library reads on the depot's clock.
        throw new IllegalStateException("TODO: toLibrary");
    }

    /** A calendar the library returned, as the moment and zone it holds. */
    ZonedDateTime fromLibrary(Calendar returned) {
        // TODO: Return the moment the library's calendar holds, in the calendar's own zone.
        //       The library may return any kind of Calendar.
        throw new IllegalStateException("TODO: fromLibrary");
    }
}
