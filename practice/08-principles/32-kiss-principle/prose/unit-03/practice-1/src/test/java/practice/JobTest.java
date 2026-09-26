package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class JobTest {

    @Test
    void followsTheLegalTransitions() throws Exception {
        Job job = new Job();
        assertThat(job.state()).isEqualTo(Job.State.IDLE);
        assertThat(job.start()).isTrue();
        assertThat(job.state()).isEqualTo(Job.State.RUNNING);
        assertThat(job.complete()).isTrue();
        assertThat(job.state()).isEqualTo(Job.State.COMPLETED);
        assertThat(job.reset()).isTrue();
        assertThat(job.state()).isEqualTo(Job.State.IDLE);
        Job failing = new Job();
        assertThat(failing.start()).isTrue();
        assertThat(failing.fail()).isTrue();
        assertThat(failing.state()).isEqualTo(Job.State.FAILED);
        assertThat(failing.reset()).isTrue();
        assertThat(failing.state()).isEqualTo(Job.State.IDLE);
    }

    @Test
    void aSecondStartIsRefused() throws Exception {
        Job job = new Job();
        assertThat(job.start()).isTrue();
        assertThat(job.start()).isFalse();
        assertThat(job.state()).isEqualTo(Job.State.RUNNING);
        job.complete();
        assertThat(job.start()).isFalse();
        assertThat(job.state()).isEqualTo(Job.State.COMPLETED);
        Job failed = new Job();
        failed.start();
        failed.fail();
        assertThat(failed.start()).as("a FAILED job is reset before it starts again").isFalse();
        assertThat(failed.state()).isEqualTo(Job.State.FAILED);
    }

    @Test
    void finishingNeedsARunningJob() throws Exception {
        Job job = new Job();
        assertThat(job.complete()).isFalse();
        assertThat(job.state()).isEqualTo(Job.State.IDLE);
        assertThat(job.fail()).isFalse();
        assertThat(job.state()).isEqualTo(Job.State.IDLE);
        job.start();
        job.fail();
        assertThat(job.complete()).isFalse();
        assertThat(job.fail()).as("fail() on a FAILED job").isFalse();
        assertThat(job.state()).isEqualTo(Job.State.FAILED);
        Job done = new Job();
        done.start();
        done.complete();
        assertThat(done.complete()).as("complete() on a COMPLETED job").isFalse();
        assertThat(done.fail()).isFalse();
        assertThat(done.state()).isEqualTo(Job.State.COMPLETED);
    }

    @Test
    void resetOnlyFromAFinishedState() throws Exception {
        Job job = new Job();
        assertThat(job.reset()).isFalse();
        assertThat(job.state()).isEqualTo(Job.State.IDLE);
        job.start();
        assertThat(job.reset()).isFalse();
        assertThat(job.state()).isEqualTo(Job.State.RUNNING);
    }
}
