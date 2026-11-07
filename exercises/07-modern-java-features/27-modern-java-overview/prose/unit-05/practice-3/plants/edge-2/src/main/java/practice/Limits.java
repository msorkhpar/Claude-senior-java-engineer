package practice;

/** Turns raw request values into the numbers the reporting API works with. */
public final class Limits {

    /** The largest page the API serves. */
    public static final int MAX_PAGE = 200;

    private Limits() {
    }

    /**
     * The page size for a request's {@code ?size=} parameter, which the web layer has already
     * parsed with {@code Long.parseLong}: never below 1, never above {@link #MAX_PAGE}.
     */
    public static int pageSize(long requested) {
        return Math.clamp(requested, 1, MAX_PAGE);
    }

    /**
     * How full a usage gauge is drawn, from 0.0 (empty) to 1.0 (full), for {@code used} out of a
     * quota of {@code quota}. Over the quota the gauge is full; a refund can make {@code used}
     * negative, which draws it empty; an account with no quota and no usage draws it empty too.
     */
    public static double gauge(double used, double quota) {
        return Math.clamp(used / quota, 0.0, 1.0);
    }
}
