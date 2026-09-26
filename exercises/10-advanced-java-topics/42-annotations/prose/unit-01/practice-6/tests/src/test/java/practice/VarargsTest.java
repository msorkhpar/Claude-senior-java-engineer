package practice;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VarargsTest {

    @Test
    void buildsListsFromTheVarargs() {
        assertThat(Varargs.listOf("a", "b", "c")).containsExactly("a", "b", "c");
        List<List<String>> groups = new Varargs().group(new String[]{"a", "b"}, new String[]{"c"});
        assertThat(groups).hasSize(2);
        assertThat(groups.get(0)).containsExactly("a", "b");
        assertThat(groups.get(1)).containsExactly("c");
    }

    @Test
    void listOfCopiesTheArray() {
        String[] arr = {new String("first"), "second"};
        List<String> list = Varargs.listOf(arr);

        arr[0] = "z";

        assertThat(list).containsExactly("first", "second");
    }

    @Test
    void listOfKeepsNulls() {
        assertThat(Varargs.listOf("a", null)).containsExactly("a", null);
    }

    @Test
    void groupCopiesEachArray() {
        String[] first = {"a", "b"};
        List<List<String>> groups = new Varargs().group(first, new String[]{"c"});

        first[1] = "z";

        assertThat(groups.get(0)).containsExactly("a", "b");
    }

    @Test
    void bothMethodsDeclareTheirPromise() throws Exception {
        Method listOf = Varargs.class.getMethod("listOf", Object[].class);
        Method group = Varargs.class.getMethod("group", Object[][].class);

        assertThat(listOf.isAnnotationPresent(SafeVarargs.class)).isTrue();
        assertThat(group.isAnnotationPresent(SafeVarargs.class)).isTrue();
    }
}
