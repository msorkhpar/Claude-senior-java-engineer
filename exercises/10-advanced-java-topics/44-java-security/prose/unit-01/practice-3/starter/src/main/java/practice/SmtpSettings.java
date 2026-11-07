package practice;

/**
 * Connection settings for the outgoing mail relay. The service logs them at start-up, and every
 * delivery error message includes them, so that the on-call engineer can see which relay failed.
 */
public record SmtpSettings(String host, int port, String user, String password) {

    @Override
    public String toString() {
        // TODO: This is what start-up logs and delivery errors show of the settings. Make it
        //       useful to the on-call engineer and safe to paste into a ticket.
        throw new IllegalStateException("TODO: toString");
    }
}
