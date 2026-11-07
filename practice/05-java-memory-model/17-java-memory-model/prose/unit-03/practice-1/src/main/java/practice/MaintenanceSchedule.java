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
        // TODO: The status page calls this on every view while operators reschedule. The page
        //       must show a window that was really scheduled: one start and the end that went
        //       with it.
        throw new IllegalStateException("TODO: current");
    }

    /** Removes the window: current() is Window.NONE until the next reschedule. */
    public void cancel() {
        // TODO: On-call cancels the window, possibly while an operator is moving it. Afterwards
        //       there must be no window, and no half of one.
        throw new IllegalStateException("TODO: cancel");
    }
}
