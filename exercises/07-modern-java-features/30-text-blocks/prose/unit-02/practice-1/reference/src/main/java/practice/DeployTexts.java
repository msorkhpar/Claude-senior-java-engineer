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
        return """
                # m h dom mon dow command
                15 2 * * * /opt/backup/run.sh
                0 * * * * /opt/metrics/flush.sh
                0 4 * * 0 /opt/logs/rotate.sh
                """;
    }

    /** The string the gateway signs for one request: the method, the path and the date, one per line. */
    public static String stringToSign(String method, String path, String date) {
        return """
                %s
                %s
                %s""".formatted(method, path, date);
    }
}
