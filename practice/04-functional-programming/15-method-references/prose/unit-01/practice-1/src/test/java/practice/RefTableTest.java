package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class RefTableTest {

    @Test
    void eachReferenceDoesWhatItsLambdaDoes() {
        assertThat(RefTable.upper().apply("hello")).isEqualTo("HELLO");
        assertThat(RefTable.parse().apply("42")).isEqualTo(42);
        assertThat(RefTable.parse().apply("-7")).isEqualTo(-7);
        assertThat(RefTable.newList().get()).isInstanceOf(ArrayList.class).isEmpty();
        assertThat(RefTable.compare().compare("a", "a")).isZero();
        assertThat(RefTable.builder().apply("abc").toString()).isEqualTo("abc");
    }

    @Test
    void eachCallBuildsANewList() {
        Supplier<ArrayList<String>> factory = RefTable.newList();
        ArrayList<String> first = factory.get();
        first.add("x");
        ArrayList<String> second = factory.get();
        assertThat(second).isNotSameAs(first).isEmpty();
    }

    @Test
    void theFirstArgumentIsTheReceiver() {
        assertThat(RefTable.compare().compare("apple", "banana")).isNegative();
        assertThat(RefTable.compare().compare("banana", "apple")).isPositive();
    }
}
