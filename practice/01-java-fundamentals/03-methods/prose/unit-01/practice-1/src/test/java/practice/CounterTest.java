package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class CounterTest {

    @Test
    void countsClicks() {
        Counter.resetTotal();
        Counter c = new Counter();
        c.click();
        c.click();
        c.click();
        assertThat(c.count()).isEqualTo(3);
        assertThat(Counter.total()).isEqualTo(3);
    }

    @Test
    void eachCounterCountsItsOwnClicks() {
        Counter.resetTotal();
        Counter a = new Counter();
        Counter b = new Counter();
        a.click();
        a.click();
        b.click();
        assertThat(a.count()).isEqualTo(2);
        assertThat(b.count()).isEqualTo(1);
        assertThat(new Counter().count()).isZero();
    }

    @Test
    void theTotalCountsEveryCounter() {
        Counter.resetTotal();
        Counter a = new Counter();
        Counter b = new Counter();
        a.click();
        a.click();
        b.click();
        assertThat(Counter.total()).isEqualTo(3);
        Counter.resetTotal();
        assertThat(Counter.total()).isZero();
        assertThat(a.count()).isEqualTo(2);
    }
}
