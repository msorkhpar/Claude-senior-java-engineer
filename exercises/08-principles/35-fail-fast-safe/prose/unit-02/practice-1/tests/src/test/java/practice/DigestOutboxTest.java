package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DigestOutboxTest {

    /** A mailer that records every message, and can act while it is sending (a request arriving). */
    private static final class RecordingMailer implements DigestOutbox.Mailer {
        final List<String> sentTo = new ArrayList<>();
        final List<String> delivered = new ArrayList<>();
        DigestOutbox.Mailer whileSending = (user, notes) -> { };

        @Override
        public void send(String user, List<String> notifications) {
            sentTo.add(user);
            for (String n : notifications) delivered.add(user + ":" + n);
            whileSending.send(user, notifications);
        }
    }

    private static DigestOutbox outboxWithThreeUsers() {
        DigestOutbox outbox = new DigestOutbox();
        outbox.add("otto", "invoice 1182 is paid");
        outbox.add("marta", "build 77 failed");
        outbox.add("marta", "build 78 passed");
        outbox.add("sven", "new comment on ticket 9");
        return outbox;
    }

    @Test
    void mailsEachUserTheirWaitingNotificationsInOneMessage() {
        DigestOutbox outbox = outboxWithThreeUsers();
        RecordingMailer mailer = new RecordingMailer();

        int mailed = outbox.sendDigests(mailer);

        assertThat(mailed).isEqualTo(3);
        assertThat(mailer.sentTo).containsExactlyInAnyOrder("otto", "marta", "sven");
        assertThat(mailer.delivered).containsExactlyInAnyOrder("otto:invoice 1182 is paid",
                "marta:build 77 failed", "marta:build 78 passed", "sven:new comment on ticket 9");
        assertThat(outbox.waiting()).isEmpty();
    }

    @Test
    void aRunWithNothingWaitingMailsNobody() {
        DigestOutbox outbox = new DigestOutbox();
        RecordingMailer mailer = new RecordingMailer();

        assertThat(outbox.sendDigests(mailer)).isZero();
        assertThat(mailer.sentTo).isEmpty();
    }

    @Test
    void aNotificationForAUserAlreadyMailedInThisRunStillReachesThem() {
        DigestOutbox outbox = outboxWithThreeUsers();
        RecordingMailer mailer = new RecordingMailer();
        mailer.whileSending = (user, notes) -> {
            if (mailer.sentTo.size() == 2) outbox.add(mailer.sentTo.get(0), "late arrival");
        };

        outbox.sendDigests(mailer);
        String first = mailer.sentTo.get(0);
        mailer.whileSending = (user, notes) -> { };
        outbox.sendDigests(mailer);

        assertThat(mailer.delivered).containsOnlyOnce(first + ":late arrival");
    }

    @Test
    void aNotificationArrivingWhileItsUserIsBeingMailedStillReachesThem() {
        DigestOutbox outbox = outboxWithThreeUsers();
        RecordingMailer mailer = new RecordingMailer();
        mailer.whileSending = (user, notes) -> {
            if (mailer.sentTo.size() == 1) outbox.add(user, "arrived during the send");
        };

        outbox.sendDigests(mailer);
        String first = mailer.sentTo.get(0);
        mailer.whileSending = (user, notes) -> { };
        outbox.sendDigests(mailer);

        assertThat(mailer.delivered).contains(first + ":arrived during the send");
    }

    @Test
    void aUserMailedWhileANewNotificationArrivesDoesNotGetTheEarlierOnesAgain() {
        DigestOutbox outbox = outboxWithThreeUsers();
        RecordingMailer mailer = new RecordingMailer();
        mailer.whileSending = (user, notes) -> {
            if (mailer.sentTo.size() == 1) outbox.add(user, "arrived during the send");
        };

        outbox.sendDigests(mailer);
        mailer.whileSending = (user, notes) -> { };
        outbox.sendDigests(mailer);

        assertThat(mailer.delivered).doesNotHaveDuplicates();
        assertThat(outbox.waiting()).isEmpty();
    }
}
