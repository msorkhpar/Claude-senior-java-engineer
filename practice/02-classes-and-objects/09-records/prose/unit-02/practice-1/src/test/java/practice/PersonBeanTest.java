package practice;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PersonBeanTest {

    @Test
    void behavesLikeTheRecord() {
        PersonBean alice = new PersonBean("Alice", 30);
        assertThat(alice.equals(new PersonBean("Alice", 30))).isTrue();
        assertThat(alice.equals(new PersonBean("Alice", 31))).isFalse();
        assertThat(alice.equals(new PersonBean("David", 30))).isFalse();
        assertThat(alice.toString()).isEqualTo("PersonBean[name=Alice, age=30]");
    }

    @Test
    void aNullNameIsHandled() {
        PersonBean nameless = new PersonBean(null, 5);
        assertThat(nameless.equals(new PersonBean(null, 5))).isTrue();
        assertThat(nameless.equals(new PersonBean(new String("Alice"), 5))).isFalse();
        assertThat(new PersonBean(new String("Alice"), 5).equals(nameless)).isFalse();
        assertThat(nameless.hashCode()).isEqualTo(new PersonBean(null, 5).hashCode());
    }

    @Test
    void equalBeansHashAlike() {
        PersonBean a = new PersonBean(new String("Charlie"), 40);
        PersonBean b = new PersonBean(new String("Charlie"), 40);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        Set<PersonBean> set = new HashSet<>();
        set.add(a);
        set.add(b);
        assertThat(set.size()).isEqualTo(1);
    }

    @Test
    void otherTypesAndNullAreNeverEqual() {
        PersonBean alice = new PersonBean(new String("Alice"), 30);
        assertThat(alice.equals(null)).isFalse();
        assertThat(alice.equals("Alice")).isFalse();
    }

    @Test
    void equalNamesBuiltSeparatelyAreEqual() {
        String first = new StringBuilder("Al").append("ice").toString();
        String second = new String("Alice");
        assertThat(new PersonBean(first, 30).equals(new PersonBean(second, 30))).isTrue();
        assertThat(new PersonBean(first, 30).hashCode()).isEqualTo(new PersonBean(second, 30).hashCode());
    }
}
