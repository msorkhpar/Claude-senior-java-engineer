package practice;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestMethodOrder(MethodOrderer.MethodName.class)
class LazyDefaultTest {

    @Test
    void generatesAnEmptyValue() {
        List<String> list = LazyDefault.EMPTY_LIST.generate();
        Map<String, Integer> map = LazyDefault.EMPTY_MAP.generateAs(Map.class);
        StringBuilder buffer = LazyDefault.BUFFER.generate();
        assertThat(list).isEmpty();
        assertThat(map).isEmpty();
        assertThat(buffer.length()).isZero();
    }

    @Test
    void newValueOnEveryCall() {
        List<String> first = LazyDefault.EMPTY_LIST.generate();
        first.add("x");
        List<String> second = LazyDefault.EMPTY_LIST.generate();
        assertThat(second).isEmpty();
        assertThat(second).isNotSameAs(first);
        StringBuilder a = LazyDefault.BUFFER.generate();
        a.append("used");
        StringBuilder b = LazyDefault.BUFFER.generate();
        assertThat(b.length()).isZero();
        Map<String, Integer> m1 = LazyDefault.EMPTY_MAP.generate();
        m1.put("k", 1);
        Map<String, Integer> m2 = LazyDefault.EMPTY_MAP.generate();
        assertThat(m2).isEmpty();
        Map<String, Integer> m3 = LazyDefault.EMPTY_MAP.generateAs(Map.class);
        m3.put("k", 1);
        Map<String, Integer> m4 = LazyDefault.EMPTY_MAP.generateAs(Map.class);
        assertThat(m4).isEmpty();
    }

    @Test
    void typeIsCheckedByGenerateAs() {
        assertThatThrownBy(() -> LazyDefault.EMPTY_LIST.generateAs(Map.class)).isInstanceOf(ClassCastException.class);
    }
}
