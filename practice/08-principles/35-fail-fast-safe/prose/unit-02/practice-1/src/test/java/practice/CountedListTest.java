package practice;

import org.junit.jupiter.api.Test;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.*;

class CountedListTest {

    @Test
    void addAndRemoveAreStructural() {
        CountedList<String> list = new CountedList<>();
        list.add("a");
        list.add("b");
        assertThat(list.modCount()).isEqualTo(2);
        assertThat(list.remove(0)).isEqualTo("a");
        assertThat(list.modCount()).isEqualTo(3);
        assertThat(list.size()).isEqualTo(1);
        Iterator<String> it = list.iterator();
        it.next();
        list.add("c");
        assertThatThrownBy(it::next).isInstanceOf(ConcurrentModificationException.class);
    }

    @Test
    void setDuringATraversalIsAllowed() {
        CountedList<String> list = new CountedList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        int before = list.modCount();
        int i = 0;
        for (String s : list) {
            assertThat(list.set(i++, s.toUpperCase())).isEqualTo(s);
        }
        assertThat(list.modCount()).isEqualTo(before);
        assertThat(list.get(0) + list.get(1) + list.get(2)).isEqualTo("ABC");
    }

    @Test
    void clearIsStructural() {
        CountedList<String> list = new CountedList<>();
        list.add("a");
        list.add("b");
        Iterator<String> it = list.iterator();
        it.next();
        int before = list.modCount();
        list.clear();
        assertThat(list.size()).isZero();
        assertThat(list.modCount()).isEqualTo(before + 1);
        assertThatThrownBy(it::next).isInstanceOf(ConcurrentModificationException.class);
        CountedList<String> empty = new CountedList<>();
        empty.clear();
        assertThat(empty.modCount()).isEqualTo(1);
    }

    @Test
    void writesWorkByPosition() {
        CountedList<String> list = new CountedList<>();
        list.add("a");
        list.add("b");
        list.add("a");
        assertThat(list.remove(2)).isEqualTo("a");
        assertThat(list.get(0) + list.get(1)).isEqualTo("ab");
        list.add("a");
        assertThat(list.set(2, "c")).isEqualTo("a");
        assertThat(list.get(0) + list.get(1) + list.get(2)).isEqualTo("abc");
    }

    @Test
    void aFailedRemoveChangesNothing() {
        CountedList<String> list = new CountedList<>();
        list.add("a");
        int before = list.modCount();
        assertThatThrownBy(() -> list.remove(5)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThat(list.modCount()).isEqualTo(before);
        assertThat(list.size()).isEqualTo(1);
    }
}
