package practice;

/**
 * The maintenance window that the status page shows and the deploy gate obeys. Operators
 * reschedule it from the admin console, on-call can cancel it, and every page view reads it,
 * each from its own request thread.
 */
public class MaintenanceSchedule {

    /** A window in minutes after midnight UTC: start before end, or NONE when there is no window. */
    public record Window(int startMinute, int endMinute) {
        public static final Window NONE = new Window(-1, -1);
    }

    private final Object lock = new Object();
    private final Runnable midReschedule;
    private int startMinute = 120;                  // 02:00
    private int endMinute = 180;                    // 03:00

    /**
     * {@code midReschedule} runs inside {@link #reschedule} between its two writes. The tests use
     * it to pause a reschedule halfway; production passes a no-op.
     */
    public MaintenanceSchedule(Runnable midReschedule) {
        this.midReschedule = midReschedule;
    }

    public MaintenanceSchedule() {
        this(() -> { });
    }

    /** Moves the window. */
    public void reschedule(int startMinute, int endMinute) {
        if (startMinute < 0 || startMinute >= endMinute) {
            throw new IllegalArgumentException("start must be before end: " + startMinute + ".." + endMinute);
        }
        synchronized (lock) {
            this.startMinute = startMinute;
            midReschedule.run();
            this.endMinute = endMinute;
        }
    }

    /** The window as it stands now. */
    public Window current() {
        synchronized (lock) {
            return new Window(startMinute, endMinute);
        }
    }

    /** Removes the window: current() is Window.NONE until the next reschedule. */
    public void cancel() {
        synchronized (lock) {
            startMinute = -1;
            endMinute = -1;
        }
    }
}
