package practice;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

/** Runs in name order, so the test that fills a created collection runs after the main case. */
@TestMethodOrder(MethodOrderer.MethodName.class)
class CollectionFactoryTest {

    @Test
    void createFromCopiesTheSource() {
        List<String> source = new ArrayList<>(List.of("a", "b"));
        Collection<String> copy = CollectionFactory.ARRAY_LIST.createFrom(source);
        source.add("x");
        assertThat(copy).containsExactly("a", "b");
        HashSet<String> hashed = new HashSet<>(List.of("a"));
        Collection<String> hashCopy = CollectionFactory.HASH_SET.createFrom(hashed);
        hashed.add("x");
        assertThat(hashCopy).containsExactly("a");
        TreeSet<String> sorted = new TreeSet<>(List.of("a"));
        Collection<String> treeCopy = CollectionFactory.TREE_SET.createFrom(sorted);
        sorted.add("x");
        assertThat(treeCopy).containsExactly("a");
    }

    @Test
    void createGivesANewCollectionEachTime() {
        Collection<String> first = CollectionFactory.ARRAY_LIST.create();
        first.add("used");
        Collection<String> second = CollectionFactory.ARRAY_LIST.create();
        assertThat(second).isEmpty();
        for (CollectionFactory kind : List.of(CollectionFactory.HASH_SET, CollectionFactory.TREE_SET)) {
            Collection<String> one = kind.create();
            one.add("used");
            assertThat(kind.<String>create()).as(kind.name()).isEmpty();
        }
    }

    @Test
    void buildsTypedCollectionsOfEachKind() {
        Collection<String> list = CollectionFactory.ARRAY_LIST.of("b", "a", "b");
        Collection<Integer> empty = CollectionFactory.HASH_SET.create();
        Collection<String> tree = CollectionFactory.TREE_SET.createFrom(List.of("pear", "apple"));
        assertThat(list).isInstanceOf(ArrayList.class).containsExactly("b", "a", "b");
        assertThat(empty).isInstanceOf(HashSet.class).isEmpty();
        assertThat(tree).isInstanceOf(TreeSet.class).containsExactly("apple", "pear");
    }

    @Test
    void ofFillsTheConstantsOwnKind() {
        assertThat(CollectionFactory.HASH_SET.of(1, 2, 3, 2, 1)).hasSize(3);
        assertThat(CollectionFactory.TREE_SET.of("pear", "apple", "fig")).containsExactly("apple", "fig", "pear");
    }
}
