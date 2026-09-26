package practice;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import practice.Notifications.MessageFormatter;
import practice.Notifications.NotificationSender;
import practice.Notifications.NotificationService;
import practice.Notifications.TemplateFormatter;

import static org.assertj.core.api.Assertions.*;

class NotificationsTest {

    /** An in-memory test double: records "recipient: message", and refuses any recipient named "nobody". */
    static final class RecordingSender implements NotificationSender {
        final List<String> sent = new CopyOnWriteArrayList<>();

        @Override
        public boolean send(String recipient, String message) {
            if (recipient.equals("nobody")) {
                return false;
            }
            sent.add(recipient + ": " + message);
            return true;
        }
    }

    static String s(String v) {
        return new String(v);
    }

    @Test
    void formatsAndSends() throws Exception {
        RecordingSender sender = new RecordingSender();
        NotificationService service = new NotificationService(sender, new TemplateFormatter());
        assertThat(service.notify(s("admin"), s("Alert: ${event}"), Map.of(s("event"), s("login")))).isTrue();
        assertThat(sender.sent).containsExactly("admin: Alert: login");
        assertThat(service.getSuccessCount()).isEqualTo(1);
        assertThat(service.getFailureCount()).isZero();
    }

    @Test
    void aRefusedSendIsCountedAsAFailure() throws Exception {
        RecordingSender sender = new RecordingSender();
        NotificationService service = new NotificationService(sender, new TemplateFormatter());
        assertThat(service.notify(s("nobody"), s("Hi"), Map.of())).isFalse();
        assertThat(service.notify(s("ann"), s("Hi"), Map.of())).isTrue();
        assertThat(service.getSuccessCount()).isEqualTo(1);
        assertThat(service.getFailureCount()).isEqualTo(1);
    }

    @Test
    void theServiceUsesTheFormatterItIsGiven() throws Exception {
        RecordingSender sender = new RecordingSender();
        MessageFormatter shouting = (template, vars) -> template.toUpperCase();
        NotificationService service = new NotificationService(sender, shouting);
        service.notify(s("bob"), s("hello ${name}"), Map.of(s("name"), s("Bob")));
        assertThat(sender.sent).containsExactly("bob: HELLO ${NAME}");
    }

    @Test
    void everyPlaceholderIsReplaced() throws Exception {
        String text = new TemplateFormatter().format(s("Hi ${name}, bye ${name}! ${missing}"), Map.of(s("name"), s("Ann")));
        assertThat(text).isEqualTo("Hi Ann, bye Ann! ${missing}");
        Map<String, String> vars = new HashMap<>();
        vars.put(s("price"), s("$5"));
        vars.put(s("a"), s("${b}"));
        vars.put(s("b"), s("x"));
        vars.put(s("user.name"), s("Ann"));
        assertThat(new TemplateFormatter().format(s("${user.name} pays ${price} for ${a}"), vars))
                .as("values are inserted as they are, and a key may hold a dot")
                .isEqualTo("Ann pays $5 for ${b}");
    }
}
