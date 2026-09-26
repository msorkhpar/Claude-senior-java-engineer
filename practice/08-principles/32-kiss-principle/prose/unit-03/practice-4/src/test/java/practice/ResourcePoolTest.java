package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ResourcePoolTest {

    @Test
    void acquiresAndReleases() throws Exception {
        ResourcePool pool = new ResourcePool(3);
        assertThat(pool.available()).isEqualTo(3);
        assertThat(pool.tryAcquire()).isTrue();
        assertThat(pool.available()).isEqualTo(2);
        pool.release();
        assertThat(pool.available()).isEqualTo(3);
    }

    @Test
    void aFullPoolRefuses() throws Exception {
        ResourcePool pool = new ResourcePool(2);
        assertThat(pool.tryAcquire()).isTrue();
        assertThat(pool.tryAcquire()).isTrue();
        assertThat(pool.tryAcquire()).isFalse();
        assertThat(pool.available()).isZero();
        pool.release();
        assertThat(pool.tryAcquire()).isTrue();
    }

    @Test
    void aNonPositiveCapacityIsRefused() throws Exception {
        assertThatThrownBy(() -> new ResourcePool(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ResourcePool(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anExtraReleaseIsRefused() throws Exception {
        ResourcePool pool = new ResourcePool(2);
        assertThatThrownBy(pool::release).isInstanceOf(IllegalStateException.class);
        assertThat(pool.available()).isEqualTo(2);
        assertThat(pool.tryAcquire()).isTrue();
        pool.release();
        assertThatThrownBy(pool::release).isInstanceOf(IllegalStateException.class);
        assertThat(pool.available()).isEqualTo(2);
    }

    @Test
    void aFreeResourceIsTakenWithoutWaiting() throws Exception {
        ResourcePool pool = new ResourcePool(1);
        Thread.currentThread().interrupt();
        boolean got;
        try {
            got = pool.tryAcquire();
        } finally {
            Thread.interrupted();
        }
        assertThat(got).as("a free resource is taken at once, even with the interrupt flag set").isTrue();
        assertThat(pool.available()).isZero();
    }
}
