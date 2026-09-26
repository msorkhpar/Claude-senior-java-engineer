package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

class FactoriesTest {

    @Test
    void buildsWithTheGivenConstructors() {
        List<ArrayList<String>> lists = Factories.buckets(3, ArrayList::new);
        assertThat(lists).hasSize(3).allSatisfy(b -> assertThat(b).isEmpty());
        List<TreeSet<String>> sets = Factories.buckets(2, TreeSet::new);
        assertThat(sets).hasSize(2).allSatisfy(b -> assertThat(b).isInstanceOf(TreeSet.class).isEmpty());
        assertThat(Factories.buckets(0, ArrayList::new)).isEmpty();

        List<StringBuilder> builders = Factories.build(List.of("Alice", "Bob"), StringBuilder::new);
        assertThat(builders).extracting(StringBuilder::toString).containsExactly("Alice", "Bob");
    }

    @Test
    void everyBucketIsItsOwnObject() {
        List<ArrayList<String>> lists = Factories.buckets(3, ArrayList::new);
        lists.get(0).add("x");
        assertThat(lists.get(1)).isEmpty();
        assertThat(lists.get(2)).isEmpty();
    }

    @Test
    void shoutGivesAStringArray() {
        String[] shouted = Factories.shout(List.of("a", "b"));
        assertThat(shouted).containsExactly("A", "B");
        assertThat(shouted.getClass().getComponentType()).isEqualTo(String.class);
    }
}
