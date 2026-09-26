package practice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Timeout(value = 10, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
class VersionedValueTest {

    @Test
    void setsAndComparesWithStamps() {
        VersionedValue value = new VersionedValue("A");
        assertThat(value.value()).isEqualTo("A");
        assertThat(value.stamp()).isZero();
        assertThat(value.compareAndSet("A", 0, "C")).isTrue();
        assertThat(value.value()).isEqualTo("C");
        assertThat(value.stamp()).isEqualTo(1);
        value.set("D");
        assertThat(value.value()).isEqualTo("D");
        assertThat(value.stamp()).isEqualTo(2);
    }

    @Test
    void aValueThatCameBackIsStillAChange() {
        VersionedValue value = new VersionedValue("A");
        int seen = value.stamp();
        value.set("B");
        value.set("A");
        assertThat(value.compareAndSet("A", seen, "C")).as("the CAS that expects the old stamp").isFalse();
        assertThat(value.value()).isEqualTo("A");
        assertThat(value.compareAndSet("A", value.stamp(), "C")).as("the CAS that expects the new stamp").isTrue();
        assertThat(value.value()).isEqualTo("C");
    }

    @Test
    void anEqualButDifferentObjectDoesNotMatch() {
        String current = new String("A");
        VersionedValue value = new VersionedValue(current);
        String lookalike = new StringBuilder().append('A').toString();
        assertThat(value.compareAndSet(lookalike, 0, "C")).as("a CAS expecting an equal but different object").isFalse();
        assertThat(value.value()).isSameAs(current);
        assertThat(value.compareAndSet(current, 0, "C")).as("a CAS expecting the very object").isTrue();
    }

    @Test
    void aFailedCompareAndSetChangesNothing() {
        VersionedValue value = new VersionedValue("A");
        value.set("B");
        assertThat(value.compareAndSet("B", 0, "C")).isFalse();
        assertThat(value.value()).isEqualTo("B");
        assertThat(value.stamp()).isEqualTo(1);
    }
}
