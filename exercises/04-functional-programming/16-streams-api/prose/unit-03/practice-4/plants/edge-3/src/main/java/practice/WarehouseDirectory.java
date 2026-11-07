package practice;

import java.util.List;
import java.util.Set;

/**
 * The admin console lists the company's warehouse codes, a page at a time, in alphabetical order.
 * The codes come from the inventory service as a Set, in no particular order; the console runs
 * on several instances, and a user's next page may be served by any of them.
 */
public class WarehouseDirectory {

    private final Set<String> codes;

    public WarehouseDirectory(Set<String> codes) {
        this.codes = codes;
    }

    /** How many pages of {@code size} codes the console offers. */
    public int pageCount(int size) {
        return (codes.size() + size - 1) / size;
    }

    /** Page {@code page} (from 0) of {@code size} codes, in alphabetical order. */
    public List<String> page(int page, int size) {
        return codes.stream()
                .sorted()
                .limit(size)
                .skip((long) page * size)
                .toList();
    }
}
