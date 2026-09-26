package practice;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

class TinyListTest {

    @Test
    void iteratesInOrder() {
        TinyList<String> list = new TinyList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        List<String> seen = new ArrayList<>();
        for (String s : list) {
            seen.add(s);
        }
        assertThat(seen).containsExactly("a", "b", "c");
        Iterator<String> it = list.iterator();
        it.next();
        it.next();
        it.next();
        assertThat(it.hasNext()).isFalse();
        assertThatThrownBy(it::next).isInstanceOf(NoSuchElementException.class);
        assertThat(new TinyList<String>().iterator().hasNext()).isFalse();
    }

    @Test
    void aWriteOutsideTheIteratorFailsTheNextNext() {
        TinyList<String> list = new TinyList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        Iterator<String> it = list.iterator();
        assertThat(it.next()).isEqualTo("a");
        list.add("d");
        assertThatThrownBy(it::next).isInstanceOf(ConcurrentModificationException.class);
        Iterator<String> it2 = list.iterator();
        it2.next();
        list.removeAt(3);
        assertThatThrownBy(it2::next).isInstanceOf(ConcurrentModificationException.class);
        // an add and a remove leave the size as it was, and still count as two changes
        Iterator<String> it3 = list.iterator();
        it3.next();
        list.add("x");
        list.removeAt(list.size() - 1);
        assertThatThrownBy(it3::next).isInstanceOf(ConcurrentModificationException.class);
        // a change before the first next() counts: the copy is taken when the iterator is created
        Iterator<String> it4 = list.iterator();
        list.add("y");
        assertThatThrownBy(it4::next).isInstanceOf(ConcurrentModificationException.class);
        // the check comes first, even when the change leaves the cursor at the end
        TinyList<String> abc = new TinyList<>();
        abc.add("a");
        abc.add("b");
        abc.add("c");
        Iterator<String> it5 = abc.iterator();
        it5.next();
        it5.next();
        abc.removeAt(2);
        assertThatThrownBy(it5::next).isInstanceOf(ConcurrentModificationException.class);
    }

    @Test
    void removeAlsoChecksForOutsideChanges() {
        TinyList<String> list = new TinyList<>();
        list.add("a");
        list.add("b");
        Iterator<String> it = list.iterator();
        it.next();
        list.add("c");
        assertThatThrownBy(it::remove).isInstanceOf(ConcurrentModificationException.class);
        assertThat(list.size()).isEqualTo(3);
        assertThat(list.get(0)).isEqualTo("a");
    }

    @Test
    void iteratorRemoveKeepsTheIteratorInStep() {
        TinyList<String> list = new TinyList<>();
        for (String s : List.of("a", "b", "c", "d")) {
            list.add(s);
        }
        List<String> seen = new ArrayList<>();
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String s = it.next();
            seen.add(s);
            if (s.equals("b") || s.equals("c")) {
                it.remove();
            }
        }
        assertThat(seen).containsExactly("a", "b", "c", "d");
        assertThat(list.size()).isEqualTo(2);
        assertThat(list.get(0)).isEqualTo("a");
        assertThat(list.get(1)).isEqualTo("d");
    }

    @Test
    void removeNeedsANextBeforeIt() {
        TinyList<String> list = new TinyList<>();
        list.add("a");
        list.add("b");
        Iterator<String> it = list.iterator();
        assertThatThrownBy(it::remove).isInstanceOf(IllegalStateException.class);
        assertThat(list.size()).isEqualTo(2);
        it.next();
        it.remove();
        assertThatThrownBy(it::remove).isInstanceOf(IllegalStateException.class);
        assertThat(list.size()).isEqualTo(1);
        assertThat(list.get(0)).isEqualTo("b");
    }
}
