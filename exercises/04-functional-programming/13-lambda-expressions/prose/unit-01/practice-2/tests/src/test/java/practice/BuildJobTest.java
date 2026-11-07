package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

import static org.assertj.core.api.Assertions.assertThat;

class BuildJobTest {

    private final List<String> channel = new ArrayList<>();
    private final BuildJob job = new BuildJob(731, "release/7.2", channel::add);

    @Test
    void progressReportsNameTheBuildAndThePercentage() {
        IntConsumer progress = job.progressCallback();

        progress.accept(25);
        progress.accept(80);

        assertThat(channel).containsExactly("build #731 on release/7.2: 25%", "build #731 on release/7.2: 80%");
    }

    @Test
    void theCompletionCallbackPostsOneLineWhenItRuns() {
        Runnable done = job.completionCallback();
        assertThat(channel).isEmpty();

        done.run();

        assertThat(channel).hasSize(1);
    }

    @Test
    void theCompletionLineNamesTheBuild() {
        job.setStatus("passed");
        Runnable done = job.completionCallback();

        done.run();

        assertThat(channel).singleElement().asString().startsWith("build #731 on release/7.2: ");
    }

    @Test
    void theCompletionLineSaysHowTheBuildEnded() {
        Runnable done = job.completionCallback();
        job.setStatus("running");
        job.setStatus("failed: 3 tests");

        done.run();

        assertThat(channel).singleElement().asString().endsWith(": failed: 3 tests");
    }
}
