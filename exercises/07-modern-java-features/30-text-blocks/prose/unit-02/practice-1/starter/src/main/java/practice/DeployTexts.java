package practice;

/**
 * Texts the deploy tool produces: the crontab it installs on every host, and the string it signs
 * for each request to the artifact gateway.
 */
public final class DeployTexts {

    private DeployTexts() {
    }

    /**
     * The crontab installed on every host. Its four lines, in this order:
     * <pre>
     * # m h dom mon dow command
     * 15 2 * * * /opt/backup/run.sh
     * 0 * * * * /opt/metrics/flush.sh
     * 0 4 * * 0 /opt/logs/rotate.sh
     * </pre>
     */
    public static String crontab() {
        // TODO: The host's installer only accepts a crontab whose every line, the last one
        //       included, ends with a line break.
        throw new IllegalStateException("TODO: crontab");
    }

    /** The string the gateway signs for one request: the method, the path and the date, one per line. */
    public static String stringToSign(String method, String path, String date) {
        // TODO: The gateway computes its signature over exactly these characters and nothing
        //       after the date.
        throw new IllegalStateException("TODO: stringToSign");
    }
}
