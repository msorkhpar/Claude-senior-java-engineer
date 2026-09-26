package practice;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class NotifyTest {

    @Test
    void theManagerSendsThroughEmail() {
        var email = new Notify.EmailSender();
        var manager = new Notify.NotificationManager(email);
        manager.notifyUser("alice", "hi");
        manager.notifyUser("bob", "your order shipped");
        assertThat(email.sentMessages()).containsExactly("EMAIL[alice]: hi", "EMAIL[bob]: your order shipped");
        var exact = new Notify.EmailSender();
        var exactManager = new Notify.NotificationManager(exact);
        exactManager.notifyUser("Alice", "Hi");
        exactManager.notifyUser("Alice", "Hi");
        exactManager.notifyUser(" x ", " hi ");
        assertThat(exact.sentMessages()).containsExactly("EMAIL[Alice]: Hi", "EMAIL[Alice]: Hi", "EMAIL[ x ]:  hi ");
    }

    @Test
    void anSmsSenderSwapsInWithNoChange() {
        var sms = new Notify.SmsSender();
        new Notify.NotificationManager(sms).notifyUser("carol", "code 4711");
        assertThat(sms.sentMessages()).containsExactly("SMS[carol]: code 4711");
    }

    @Test
    void aSenderWrittenLaterReceivesEveryMessage() {
        List<String> received = new ArrayList<>();
        Notify.MessageSender push = (to, message) -> received.add(to + " <- " + message);
        var manager = new Notify.NotificationManager(push);
        manager.notifyUser("dave", "ping");
        manager.notifyUser("erin", "pong");
        assertThat(received).containsExactly("dave <- ping", "erin <- pong");
    }

    @Test
    void aNullSenderIsRejectedAtConstruction() {
        new Notify.EmailSender().send("x", "y");
        assertThatNullPointerException().isThrownBy(() -> new Notify.NotificationManager(null));
    }
}
