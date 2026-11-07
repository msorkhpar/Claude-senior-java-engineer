package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OutageNotifierTest {

    private static final int[] NONE = {};

    private final OutageNotifier notifier = new OutageNotifier();

    @Test
    void listsEveryContactInCustomerOrder() {
        List<OutageNotifier.Customer> segment = List.of(
                new OutageNotifier.Customer("Jane Doe", "jane@example.com", "+1-555-0101", NONE),
                new OutageNotifier.Customer("John Roe", "john@example.com", "+1-555-0102", NONE));

        assertThat(notifier.contacts(segment))
                .containsExactly("jane@example.com", "+1-555-0101", "john@example.com", "+1-555-0102");
    }

    @Test
    void anHourOutsideTheQuietHoursIsNotQuiet() {
        assertThat(notifier.isQuietHour(new int[] {1, 2, 3}, 14)).isFalse();
        assertThat(notifier.isQuietHour(NONE, 3)).isFalse();
    }

    @Test
    void aMissingContactIsLeftOut() {
        List<OutageNotifier.Customer> segment = List.of(
                new OutageNotifier.Customer("Ann Poe", null, "+1-555-0103", NONE),
                new OutageNotifier.Customer("Bo Moe", "bo@example.com", null, NONE));

        assertThat(notifier.contacts(segment)).containsExactly("+1-555-0103", "bo@example.com");
    }

    @Test
    void aSegmentTheCrmReturnedAsNullHasNoContacts() {
        List<String> sent = new ArrayList<>();

        assertThat(notifier.contacts(null)).isEmpty();
        assertThat(notifier.notify(null, 9, sent::add)).isZero();
    }

    @Test
    void aQuietHourIsRecognised() {
        assertThat(notifier.isQuietHour(new int[] {22, 23, 0, 1}, 23)).isTrue();

        List<String> sent = new ArrayList<>();
        notifier.notify(List.of(new OutageNotifier.Customer("Cy Loe", "cy@example.com", null, new int[] {7, 8})),
                8, sent::add);
        assertThat(sent).isEmpty();
    }
}
