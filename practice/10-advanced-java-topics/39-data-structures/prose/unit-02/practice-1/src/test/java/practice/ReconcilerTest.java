package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReconcilerTest {

    /** Counts the equals() and hashCode() calls made on its items. */
    static final class Calls {
        long equals;
        long hashes;
    }

    /** A pallet tag; equal when the codes are equal. */
    static final class Tag {
        final String code;
        final Calls calls;

        Tag(String code, Calls calls) {
            this.code = code;
            this.calls = calls;
        }

        @Override
        public boolean equals(Object o) {
            calls.equals++;
            return o instanceof Tag t && t.code.equals(code);
        }

        @Override
        public int hashCode() {
            calls.hashes++;
            return code.hashCode();
        }

        @Override
        public String toString() {
            return code;
        }
    }

    private static List<Tag> tags(Calls calls, String... codes) {
        List<Tag> tags = new ArrayList<>();
        for (String c : codes) tags.add(new Tag(new String(c), calls));
        return tags;
    }

    /** n shipped tags; every third one was never billed. Billed tags are distinct objects. */
    private static List<List<Tag>> month(Calls calls, int n) {
        List<Tag> shipped = new ArrayList<>(), billed = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String code = "PAL-" + (i * 7 + 3);
            shipped.add(new Tag(code, calls));
            if (i % 3 != 0) billed.add(new Tag(new String(code), calls));
        }
        return List.of(shipped, billed);
    }

    @Test
    void findsTheUnbilledTagsInShippingOrderWithRepeats() {
        Calls calls = new Calls();
        List<Tag> shipped = tags(calls, "T-9", "T-4", "T-9", "T-12", "T-5", "T-4");
        List<Tag> billed = tags(calls, "T-12", "T-4", "T-30");

        List<Tag> missing = Reconciler.unbilled(shipped, billed);

        assertThat(missing).extracting(t -> t.code).containsExactly("T-9", "T-9", "T-5");
        assertThat(shipped).extracting(t -> t.code).containsExactly("T-9", "T-4", "T-9", "T-12", "T-5", "T-4");
        assertThat(billed).extracting(t -> t.code).containsExactly("T-12", "T-4", "T-30");
    }

    @Test
    void reportsEveryShippedTagWhenNothingWasBilled() {
        Calls calls = new Calls();
        List<Tag> shipped = tags(calls, "B-1", "B-2");

        assertThat(Reconciler.unbilled(shipped, List.of())).extracting(t -> t.code).containsExactly("B-1", "B-2");
        assertThat(Reconciler.summary(shipped, tags(calls, "B-2"))).isEqualTo("1 of 2 shipped lines not billed");
    }

    @Test
    void aShippedTagIsNotComparedWithEveryBilledTag() {
        Calls calls = new Calls();
        List<List<Tag>> month = month(calls, 6_000);

        List<Tag> missing = Reconciler.unbilled(month.get(0), month.get(1));

        assertThat(missing).hasSize(2_000);
        assertThat(calls.equals).as("equals() calls for 6,000 shipped and 4,000 billed tags").isLessThan(30_000);
    }

    @Test
    void theBilledTagsAreHashedOnceNotOncePerShippedTag() {
        Calls calls = new Calls();
        List<List<Tag>> month = month(calls, 6_000);

        List<Tag> missing = Reconciler.unbilled(month.get(0), month.get(1));

        assertThat(missing).hasSize(2_000);
        assertThat(calls.hashes).as("hashCode() calls for 6,000 shipped and 4,000 billed tags").isLessThan(30_000);
    }
}
