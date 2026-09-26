package practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LegacyTest {

    /** An Enumeration over fixed values that counts how many elements were taken. */
    static final class CountingEnumeration implements Enumeration<String> {
        private final List<String> values;
        private int taken;

        CountingEnumeration(String... values) {
            this.values = List.of(values);
        }

        @Override
        public boolean hasMoreElements() {
            return taken < values.size();
        }

        @Override
        public String nextElement() {
            if (taken >= values.size()) {
                throw new NoSuchElementException();
            }
            return values.get(taken++);
        }
    }

    @Test
    void adaptsBothWays() {
        Iterator<String> it = Legacy.asIterator(Collections.enumeration(List.of("x", "y", "z")));
        List<String> walked = new ArrayList<>();
        while (it.hasNext()) {
            walked.add(it.next());
        }
        assertThat(walked).containsExactly("x", "y", "z");

        Enumeration<Integer> en = Legacy.asEnumeration(List.of(1000, 2000).iterator());
        List<Integer> seen = new ArrayList<>();
        while (en.hasMoreElements()) {
            seen.add(en.nextElement());
        }
        assertThat(seen).containsExactly(1000, 2000);
    }

    @Test
    void readsNothingUntilAsked() {
        CountingEnumeration source = new CountingEnumeration("a", "b", "c");

        Iterator<String> it = Legacy.asIterator(source);
        assertThat(it.hasNext()).isTrue();
        assertThat(source.taken).isZero();

        assertThat(it.next()).isEqualTo("a");
        assertThat(source.taken).isEqualTo(1);
    }

    @Test
    void pastTheEndThrowsNoSuchElement() {
        Iterator<String> it = Legacy.asIterator(new CountingEnumeration("only"));
        it.next();
        assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);

        Enumeration<String> en = Legacy.asEnumeration(List.of("only").iterator());
        en.nextElement();
        assertThatThrownBy(en::nextElement).isInstanceOf(NoSuchElementException.class);
    }
}
