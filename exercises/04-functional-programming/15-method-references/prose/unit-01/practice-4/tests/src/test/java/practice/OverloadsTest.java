package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OverloadsTest {

    @Test
    void removesByIndexAndWhereBothReadingsAgree() {
        List<Integer> byIndex = new ArrayList<>(List.of(10, 20, 30));
        Overloads.removeAt().accept(byIndex, 1);
        assertThat(byIndex).containsExactly(10, 30);

        List<Integer> agreeing = new ArrayList<>(List.of(0, 1, 2));
        Overloads.removeValue().accept(agreeing, 1);
        assertThat(agreeing).containsExactly(0, 2);
    }

    @Test
    void aBoxedArgumentRemovesTheValue() {
        List<Integer> list = new ArrayList<>(List.of(10, 2, 30, 40));
        Overloads.removeValue().accept(list, 2);
        assertThat(list).containsExactly(10, 30, 40);
    }

    @Test
    void aCharArrayBecomesItsText() {
        assertThat(Overloads.text().apply(new char[] {'a', 'b', 'c'})).isEqualTo("abc");
        assertThat(Overloads.text().apply(new char[0])).isEmpty();
    }

    @Test
    void aValueIsFoundByEqualsNotIdentity() {
        List<Integer> list = new ArrayList<>(List.of(1000, 2000, 3000));
        Overloads.removeValue().accept(list, Integer.valueOf(2000));
        assertThat(list).containsExactly(1000, 3000);
    }
}
