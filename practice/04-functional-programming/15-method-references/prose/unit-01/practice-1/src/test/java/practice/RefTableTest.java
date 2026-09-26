package practice;

import java.util.function.Function;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefTableTest {

    @Test
    void eachReferenceDoesWhatItsLambdaDoes() {
        assertThat(RefTable.upper().apply("hello")).isEqualTo("HELLO");
        assertThat(RefTable.parse().apply("42")).isEqualTo(42);
        assertThat(RefTable.parse().apply("-7")).isEqualTo(-7);
        assertThat(RefTable.parse().apply("010")).isEqualTo(10);
        assertThat(RefTable.parse().apply("08")).isEqualTo(8);
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
        assertThat(RefTable.compare().compare("B", "a")).isNegative();
    }

    @Test
    void eachBuilderIsNewAndHoldsOnlyItsText() {
        Function<String, StringBuilder> builder = RefTable.builder();
        StringBuilder first = builder.apply("ab");
        StringBuilder second = builder.apply("cd");
        assertThat(second).isNotSameAs(first);
        assertThat(first.toString()).isEqualTo("ab");
        assertThat(second.toString()).isEqualTo("cd");
        assertThatThrownBy(() -> builder.apply(null)).isInstanceOf(NullPointerException.class);
    }
}
