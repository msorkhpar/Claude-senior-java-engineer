package practice;

public final class Alerting {

    private Alerting() {
    }

    /** Is told about a failure. */
    public interface Alerter {
        void alert(RuntimeException failure);
    }

    /** Runs {@code work}; a failure is passed to {@code alerter} and then rethrown. */
    public static void run(Runnable work, Alerter alerter) {
        try {
            work.run();
        } catch (RuntimeException failure) {
            try {
                alerter.alert(failure);
            } catch (RuntimeException alertFailure) {
                // the alert is best effort
            }
            throw failure;
        }
    }
}
