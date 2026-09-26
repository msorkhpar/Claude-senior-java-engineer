package practice;

public final class Alerting {

    private Alerting() {
    }

    public interface Alerter {
        void alert(RuntimeException failure);
    }

    public static void run(Runnable work, Alerter alerter) {
        try {
            work.run();
        } catch (RuntimeException failure) {
            try {
                alerter.alert(failure);
            } catch (Throwable alertFailure) {
                failure.addSuppressed(alertFailure);
            }
            throw failure;
        }
    }
}
