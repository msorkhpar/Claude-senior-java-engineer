package practice;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class IntListTest {

    @Test
    void growsLikeThePageExample() {
        IntList list = new IntList(4);
        for (int v = 1; v <= 4; v++) {
            list.add(v);
        }
        assertThat(list.capacity()).isEqualTo(4);
        assertThat(list.copies()).isZero();

        list.add(5);
        assertThat(list.capacity()).isEqualTo(6);
        assertThat(list.copies()).isEqualTo(4);

        list.add(6);
        assertThat(list.capacity()).isEqualTo(6);
        list.add(7);
        assertThat(list.capacity()).isEqualTo(9);
        assertThat(list.copies()).isEqualTo(10);

        assertThat(list.size()).isEqualTo(7);
        for (int i = 0; i < 7; i++) {
            assertThat(list.get(i)).isEqualTo(i + 1);
        }
        assertThatThrownBy(() -> new IntList(0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void manyAppendsCopyAFewTimesTheirNumber() {
        int n = 100_000;
        IntList list = new IntList(4);
        for (int v = 0; v < n; v++) {
            list.add(v);
        }

        assertThat(list.size()).isEqualTo(n);
        assertThat(list.get(n - 1)).isEqualTo(n - 1);
        assertThat(list.copies()).isLessThan(3L * n);
        assertThat(list.capacity()).isLessThanOrEqualTo(n + n / 2 + 1);
    }

    @Test
    void aCapacityOfOneStillGrows() {
        IntList list = new IntList(1);
        list.add(10);
        list.add(20);
        list.add(30);

        assertThat(list.size()).isEqualTo(3);
        assertThat(list.get(2)).isEqualTo(30);
        assertThat(list.capacity()).isEqualTo(3);
        assertThat(list.copies()).isEqualTo(3);
    }

    @Test
    void onlyStoredValuesCanBeRead() {
        IntList list = new IntList(4);
        list.add(42);

        assertThat(list.get(0)).isEqualTo(42);
        assertThatThrownBy(() -> list.get(1)).isInstanceOf(IndexOutOfBoundsException.class);
        assertThatThrownBy(() -> list.get(-1)).isInstanceOf(IndexOutOfBoundsException.class);
    }
}
