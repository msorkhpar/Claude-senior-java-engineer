package practice;

/**
 * Connection settings for the outgoing mail relay. The service logs them at start-up, and every
 * delivery error message includes them, so that the on-call engineer can see which relay failed.
 */
public record SmtpSettings(String host, int port, String user, String password) {

    @Override
    public String toString() {
        return "SmtpSettings[host=" + host + ", port=" + port + ", user=" + user + ", password=" + password + "]";
    }
}
