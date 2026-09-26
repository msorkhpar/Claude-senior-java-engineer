package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IntListTest {

    @Test
    void buildsSumsAndAppendsWithoutChangingTheOriginal() {
        IntList.Node list = IntList.of(1, 2, 3);
        assertThat(list).isEqualTo(new IntList.Cons(1, new IntList.Cons(2, new IntList.Cons(3, new IntList.Empty()))));
        assertThat(IntList.sum(list)).isEqualTo(6);
        IntList.Node longer = IntList.append(list, 4);
        assertThat(longer).isEqualTo(IntList.of(1, 2, 3, 4));
        assertThat(list).isEqualTo(IntList.of(1, 2, 3));
        assertThat(IntList.prepend(list, 0)).isEqualTo(IntList.of(0, 1, 2, 3));
    }

    @Test
    void noValuesMakeTheEmptyList() {
        assertThat(IntList.of()).isEqualTo(new IntList.Empty());
        assertThat(IntList.sum(IntList.of())).isZero();
    }

    @Test
    void prependSharesTheWholeOriginal() {
        IntList.Node list = IntList.of(1, 2, 3);
        IntList.Node front = IntList.prepend(list, 0);
        assertThat(front).isInstanceOf(IntList.Cons.class);
        assertThat(((IntList.Cons) front).tail()).isSameAs(list);
    }
}
