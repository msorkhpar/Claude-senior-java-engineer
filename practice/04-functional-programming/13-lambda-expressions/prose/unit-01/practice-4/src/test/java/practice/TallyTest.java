package practice;

import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class TallyTest {

    @Test
    void incrementerAddsToTheField() {
        Tally tally = new Tally();
        Runnable increment = tally.incrementer();
        increment.run();
        increment.run();
        increment.run();
        assertThat(tally.count()).isEqualTo(3);

        Runnable another = tally.incrementer();
        another.run();
        assertThat(tally.count()).isEqualTo(4);
        assertThat(new Tally().count()).isZero();
    }

    @Test
    void lambdaThisIsTheEnclosingTally() {
        Tally tally = new Tally();
        Supplier<Object> self = tally.selfFromLambda();
        assertThat(self.get()).isSameAs(tally);
    }

    @Test
    void anonymousThisIsTheSupplierItself() {
        Tally tally = new Tally();
        Supplier<Object> self = tally.selfFromAnonymousClass();
        assertThat(self.get()).isSameAs(self).isNotSameAs(tally);
    }

    @Test
    void incrementersShareTheField() {
        Tally tally = new Tally();
        Runnable first = tally.incrementer();
        Runnable second = tally.incrementer();
        first.run();
        second.run();
        first.run();
        assertThat(tally.count()).isEqualTo(3);
    }
}
