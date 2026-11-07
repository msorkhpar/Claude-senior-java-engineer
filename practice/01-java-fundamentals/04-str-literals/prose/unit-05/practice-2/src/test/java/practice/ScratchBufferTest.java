package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScratchBufferTest {

    private static List<String> invoice(String customer, int rows) {
        List<String> lines = new ArrayList<>();
        lines.add("INVOICE " + customer);
        for (int i = 1; i <= rows; i++) lines.add(String.format("%05d;WIDGET-%d;%d.%02d", i, i % 37, i % 90, i % 100));
        return lines;
    }

    @Test
    void eachInvoiceHoldsOnlyItsOwnRows() {
        ScratchBuffer buffer = new ScratchBuffer();

        String first = buffer.format(List.of("INVOICE ACME", "00001;BOLT;0.40", "00002;NUT;0.10"));
        String second = buffer.format(List.of("INVOICE GLOBEX", "00001;GEAR;12.00"));

        assertThat(first).isEqualTo("INVOICE ACME\n00001;BOLT;0.40\n00002;NUT;0.10\n");
        assertThat(second).isEqualTo("INVOICE GLOBEX\n00001;GEAR;12.00\n");
    }

    @Test
    void ordinaryInvoicesKeepTheRoomTheWorkerStartedWith() {
        ScratchBuffer buffer = new ScratchBuffer();

        for (int i = 0; i < 5; i++) buffer.format(invoice("CUSTOMER-" + i, 40));

        assertThat(buffer.capacity()).isEqualTo(ScratchBuffer.NORMAL_CAPACITY);
    }

    @Test
    void aHugeInvoiceDoesNotStayInTheWorkersMemory() {
        ScratchBuffer buffer = new ScratchBuffer();
        String huge = buffer.format(invoice("INITECH", 25_000));
        assertThat(huge.length()).isGreaterThan(ScratchBuffer.KEEP_LIMIT);

        String next = buffer.format(invoice("UMBRELLA", 12));

        assertThat(next).startsWith("INVOICE UMBRELLA\n");
        assertThat(buffer.capacity()).isLessThanOrEqualTo(ScratchBuffer.KEEP_LIMIT);
    }

    @Test
    void afterAHugeInvoiceAnOrdinaryOneFindsRoomReady() {
        ScratchBuffer buffer = new ScratchBuffer();
        buffer.format(invoice("INITECH", 25_000));

        buffer.format(invoice("UMBRELLA", 12));

        assertThat(buffer.capacity()).isGreaterThanOrEqualTo(ScratchBuffer.NORMAL_CAPACITY);
    }
}
