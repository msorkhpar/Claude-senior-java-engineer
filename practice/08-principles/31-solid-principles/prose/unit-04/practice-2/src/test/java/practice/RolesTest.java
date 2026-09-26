package practice;

import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

class RolesTest {

    @Test
    void aClientSubmitsAndAManagerShutsDown() throws Exception {
        var executor = new Roles.ManagedExecutor(2);
        var client = new Roles.TaskClient(executor);
        Thread worker = (Thread) client.runTask(Thread::currentThread).get();
        assertThat(worker).isNotSameAs(Thread.currentThread());
        assertThat(executor.getCompletedCount()).isEqualTo(1);
        CountDownLatch release = new CountDownLatch(1);
        var blocked = client.runTask(() -> release.await(30, TimeUnit.SECONDS) ? "work" : "timed out");
        assertThat(executor.getCompletedCount()).isEqualTo(1);
        release.countDown();
        assertThat(blocked.get()).isEqualTo("work");
        assertThat(executor.getCompletedCount()).isEqualTo(2);
        assertThat(client.runTask(() -> 6 * 7).get()).isEqualTo(42);
        assertThat(executor.getCompletedCount()).isEqualTo(3);
        var manager = new Roles.LifecycleManager(executor);
        assertThat(manager.isRunning()).isTrue();
        manager.gracefulShutdown();
        assertThat(manager.isRunning()).isFalse();
        assertThat(executor.isShutdown()).isTrue();
        worker.join(30_000);
        assertThat(worker.isAlive()).as("the pool's worker ends once the pool is shut down").isFalse();
    }

    @Test
    void eachClientDependsOnlyOnItsRole() throws Exception {
        var executor = new Roles.ManagedExecutor(1);
        try {
            new Roles.TaskClient(executor);
            new Roles.LifecycleManager(executor);
            assertThat(Roles.TaskClient.class.getConstructors()).singleElement()
                    .satisfies(c -> assertThat(c.getParameterTypes()).containsExactly(Roles.TaskSubmitter.class));
            assertThat(Roles.LifecycleManager.class.getConstructors()).singleElement()
                    .satisfies(c -> assertThat(c.getParameterTypes()).containsExactly(Roles.LifecycleManageable.class));
        } finally {
            executor.shutdown();
        }
    }

    @Test
    void aTaskAfterShutdownIsRefused() throws Exception {
        var executor = new Roles.ManagedExecutor(1);
        var client = new Roles.TaskClient(executor);
        new Roles.LifecycleManager(executor).gracefulShutdown();
        assertThatIllegalStateException().isThrownBy(() -> client.runTask(() -> "late"));
    }

    @Test
    void aFailedTaskIsCountedToo() throws Exception {
        var executor = new Roles.ManagedExecutor(1);
        try {
            var client = new Roles.TaskClient(executor);
            var failed = client.runTask(() -> {
                throw new IllegalStateException("boom");
            });
            assertThatThrownBy(failed::get).hasCauseInstanceOf(IllegalStateException.class);
            assertThat(executor.getCompletedCount()).isEqualTo(1);
        } finally {
            executor.shutdown();
        }
    }
}
