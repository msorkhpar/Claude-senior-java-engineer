package practice;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static org.assertj.core.api.Assertions.*;

class ImmutableArrayListTest {

    /** A string built at run time, never an interned literal. */
    private static String s(String v) {
        return new String(v);
    }

    @Test
    void readsLikeAList() {
        List<String> list = new ImmutableArrayList<>(List.of(s("red"), s("green"), s("blue")));
        assertThat(list.get(1)).isEqualTo("green");
        assertThat(list.size()).isEqualTo(3);
        assertThat(list).containsExactly("red", "green", "blue");
        assertThat(list.indexOf(s("blue"))).isEqualTo(2);
        assertThat(list).isEqualTo(new ArrayList<>(List.of(s("red"), s("green"), s("blue"))));
        List<String> withNull = new ImmutableArrayList<>(Arrays.asList(s("x"), null));
        assertThat(withNull).containsExactly("x", null);
    }

    @Test
    void itCannotBeChanged() {
        List<String> list = new ImmutableArrayList<>(List.of(s("a"), s("b")));
        assertThatThrownBy(() -> list.add(s("c"))).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> list.set(0, s("z"))).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> list.remove(0)).isInstanceOf(UnsupportedOperationException.class);
        Iterator<String> it = list.iterator();
        it.next();
        assertThatThrownBy(it::remove).isInstanceOf(UnsupportedOperationException.class);
        ListIterator<String> li = list.listIterator();
        li.next();
        assertThatThrownBy(() -> li.set(s("z"))).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> list.sort(null)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> list.replaceAll(String::toUpperCase)).isInstanceOf(UnsupportedOperationException.class);
        assertThat(list).containsExactly("a", "b");
    }

    @Test
    void laterChangesToTheSourceDoNotShow() {
        List<String> source = new ArrayList<>(List.of(s("one"), s("two")));
        List<String> list = new ImmutableArrayList<>(source);
        source.set(0, s("changed"));
        source.add(s("three"));
        assertThat(list).containsExactly("one", "two");
        Object[] handedOut = list.toArray();
        handedOut[0] = s("z");
        assertThat(list).as("an array it hands out is a copy").containsExactly("one", "two");
    }
}
