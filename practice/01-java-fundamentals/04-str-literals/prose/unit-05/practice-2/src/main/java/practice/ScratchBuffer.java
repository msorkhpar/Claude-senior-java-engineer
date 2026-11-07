package practice;

import java.util.List;

/**
 * Each invoice-export worker owns one ScratchBuffer and formats every invoice in it, one after
 * another. Workers live as long as the service.
 */
public class ScratchBuffer {

    /** Room for every ordinary invoice; the worker starts with this much. */
    static final int NORMAL_CAPACITY = 8 * 1024;

    /** A buffer that has grown past this is not worth keeping for the next invoice. */
    static final int KEEP_LIMIT = 64 * 1024;

    private StringBuilder sb = new StringBuilder(NORMAL_CAPACITY);

    /** Formats one invoice: each row on its own line. */
    public String format(List<String> rows) {
        StringBuilder out = acquire();
        for (String row : rows) out.append(row).append('\n');
        return out.toString();
    }

    /** The builder for the next invoice, empty. */
    StringBuilder acquire() {
        // TODO: Hand out the worker's builder, empty, for the next invoice. Once in a while an
        //       invoice has hundreds of thousands of lines; the memory it needed should not stay
        //       with the worker afterwards, and ordinary invoices should still find room ready.
        throw new IllegalStateException("TODO: acquire");
    }

    /** The memory this worker holds for formatting, in characters; read by the service's monitoring. */
    public int capacity() {
        return sb.capacity();
    }
}
