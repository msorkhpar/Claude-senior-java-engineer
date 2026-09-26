package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class TrackedListTest {

    private static TrackedList of(String... items) {
        TrackedList list = new TrackedList();
        for (String s : items) {
            list.add(new String(s.toCharArray()));
        }
        return list;
    }

    private static List<String> contents(TrackedList list) {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            out.add(list.get(i));
        }
        return out;
    }

    @Test
    void iteratesInOrder() {
        TrackedList list = of("a", "b", "c", "d", "e");
        List<String> seen = new ArrayList<>();
        for (String s : list) {
            seen.add(s);
        }
        assertThat(seen).containsExactly("a", "b", "c", "d", "e");
        Iterator<String> it = list.iterator();
        for (int i = 0; i < 5; i++) {
            it.next();
        }
        assertThat(it.hasNext()).isFalse();
        assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void structuralChangeFailsFast() {
        TrackedList list = of("a", "b", "c");
        Iterator<String> it = list.iterator();
        it.next();
        list.add("d");
        assertThatThrownBy(it::next).isInstanceOf(ConcurrentModificationException.class);
        Iterator<String> again = list.iterator();
        again.next();
        list.removeAt(3);
        assertThatThrownBy(again::next).isInstanceOf(ConcurrentModificationException.class);
    }

    @Test
    void setIsNotStructural() {
        TrackedList list = of("a", "b", "c");
        Iterator<String> it = list.iterator();
        assertThat(it.next()).isEqualTo("a");
        list.set(1, "B");
        assertThat(it.next()).isEqualTo("B");
        assertThat(it.next()).isEqualTo("c");
    }

    @Test
    void iteratorRemoveKeepsTheIteratorValid() {
        TrackedList list = of("a", "b", "c");
        Iterator<String> it = list.iterator();
        assertThat(it.next()).isEqualTo("a");
        it.remove();
        assertThat(it.next()).isEqualTo("b");
        assertThat(contents(list)).containsExactly("b", "c");
        assertThatThrownBy(() -> {
            it.remove();
            it.remove();
        }).isInstanceOf(IllegalStateException.class);
        assertThat(contents(list)).containsExactly("c");
    }

    @Test
    void aChangeBeforeTheFirstNextIsSeen() {
        TrackedList list = of("a", "b");
        Iterator<String> it = list.iterator();
        list.add("c");
        assertThatThrownBy(it::next).as("the iterator copied modCount when it was created, before the add")
                .isInstanceOf(ConcurrentModificationException.class);
    }

    @Test
    void removeBeforeNextIsRefused() {
        TrackedList list = of("a", "b");
        Iterator<String> it = list.iterator();
        assertThatThrownBy(it::remove).isInstanceOf(IllegalStateException.class);
        assertThat(list.size()).isEqualTo(2);
        assertThat(it.next()).isEqualTo("a");
    }
}
