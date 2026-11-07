package practice;

import org.junit.jupiter.api.Test;
import practice.Folders.Message;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FoldersTest {

    private static final Message INVOICE = new Message("billing@example.com", "Invoice 2291");
    private static final Message MEETUP = new Message("events@example.com", "Meetup on Thursday");
    private static final Message NEWSLETTER = new Message("news@example.com", "October digest");
    private static final Message RECEIPT = new Message("shop@example.com", "Receipt 77-A");

    @Test
    void returnsHowManyMessagesItMoved() {
        List<Message> inbox = new ArrayList<>(List.of(INVOICE, MEETUP, NEWSLETTER));
        List<Message> archive = new ArrayList<>(List.of(RECEIPT));

        assertThat(Folders.archiveAll(inbox, archive)).isEqualTo(3);
        assertThat(Folders.archiveAll(new ArrayList<>(), new ArrayList<>(List.of(RECEIPT)))).isZero();
    }

    @Test
    void theInboxTheScreenShowsIsEmptyAfterwards() {
        List<Message> inbox = new ArrayList<>(List.of(MEETUP, INVOICE));
        List<Message> archive = new ArrayList<>(List.of(RECEIPT));

        Folders.archiveAll(inbox, archive);

        assertThat(inbox).isEmpty();
    }

    @Test
    void theArchiveTheScreenShowsGetsTheMessagesAfterItsOwn() {
        List<Message> inbox = new ArrayList<>(List.of(NEWSLETTER, INVOICE));
        List<Message> archive = new ArrayList<>(List.of(RECEIPT, MEETUP));

        Folders.archiveAll(inbox, archive);

        assertThat(archive).containsExactly(RECEIPT, MEETUP, NEWSLETTER, INVOICE);
    }

    @Test
    void anEmptyArchiveGetsTheMessagesToo() {
        List<Message> inbox = new ArrayList<>(List.of(RECEIPT, NEWSLETTER));
        List<Message> archive = new ArrayList<>();

        Folders.archiveAll(inbox, archive);

        assertThat(archive).containsExactly(RECEIPT, NEWSLETTER);
    }
}
