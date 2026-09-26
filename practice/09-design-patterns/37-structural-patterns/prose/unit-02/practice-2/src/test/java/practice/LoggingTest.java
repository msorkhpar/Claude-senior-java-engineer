package practice;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class LoggingTest {

    /** A wrapped notifier whose result is built at run time, so no literal can match it by identity. */
    static final class Email implements Logging.Notifier {
        @Override
        public String send(String message) {
            return new StringBuilder("Email: ").append(message).toString();
        }

        @Override
        public String getDescription() {
            return "Email";
        }
    }

    static final class Failing implements Logging.Notifier {
        @Override
        public String send(String message) {
            throw new IllegalStateException("mail server down");
        }

        @Override
        public String getDescription() {
            return "Failing";
        }
    }

    @Test
    void returnsTheWrappedResultAndLogsTheSend() {
        Logging.LoggingDecorator logged = new Logging.LoggingDecorator(new Email());

        assertThat(logged.send("hi")).isEqualTo("Email: hi");
        assertThat(logged.getLog()).hasSize(2).first().isEqualTo("Sending: hi");
        assertThat(logged.getDescription()).isEqualTo("Logging(Email)");
    }

    @Test
    void theSentEntryRecordsTheWrappedResult() {
        Logging.LoggingDecorator logged = new Logging.LoggingDecorator(new Email());

        logged.send("hi");
        logged.send("bye");

        assertThat(logged.getLog()).containsExactly("Sending: hi", "Sent: Email: hi", "Sending: bye", "Sent: Email: bye");
    }

    @Test
    void sendingIsLoggedBeforeTheCall() {
        Logging.LoggingDecorator logged = new Logging.LoggingDecorator(new Failing());

        assertThatIllegalStateException().isThrownBy(() -> logged.send("hi")).withMessage("mail server down");
        assertThat(logged.getLog()).containsExactly("Sending: hi");
    }

    @Test
    void theLogCannotBeChangedFromOutside() {
        Logging.LoggingDecorator logged = new Logging.LoggingDecorator(new Email());
        logged.send("hi");

        List<String> view = logged.getLog();
        try {
            view.clear();
            view.add("forged");
        } catch (RuntimeException refused) {
            // a read-only view may refuse; either way the decorator's log must be intact
        }

        assertThat(logged.getLog()).containsExactly("Sending: hi", "Sent: Email: hi");
    }
}
