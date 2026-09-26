package practice;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import practice.Pipelines.ConcurrentPipelineExecutor;
import practice.Pipelines.ProcessingPipeline;

import static org.assertj.core.api.Assertions.*;

class PipelinesTest {

    static String s(String v) {
        return new String(v);
    }

    @Test
    void runsEachItemThroughEveryStage() throws Exception {
        ProcessingPipeline<String> pipeline = new ProcessingPipeline<String>()
                .addStage(String::trim)
                .addStage(String::toUpperCase)
                .addStage(x -> x + " [PROCESSED]");
        assertThat(pipeline.execute(s("  hello  "))).isEqualTo("HELLO [PROCESSED]");
        assertThat(new ProcessingPipeline<String>().execute(s("as is"))).isEqualTo("as is");
        ConcurrentPipelineExecutor<String> executor = new ConcurrentPipelineExecutor<>(pipeline);
        assertThat(executor.processAll(List.of(s("  hello  "), s("  world  "), s("  java  "))))
                .containsExactlyInAnyOrder("HELLO [PROCESSED]", "WORLD [PROCESSED]", "JAVA [PROCESSED]");
        assertThat(executor.processAll(List.of())).isEmpty();
        Map<String, Thread> ranOn = new ConcurrentHashMap<>();
        ProcessingPipeline<String> recording = new ProcessingPipeline<String>()
                .addStage(x -> { ranOn.put(x, Thread.currentThread()); return x; });
        new ConcurrentPipelineExecutor<>(recording).processAll(List.of(s("p"), s("q"), s("r")));
        assertThat(ranOn).hasSize(3);
        assertThat(ranOn.values()).as("each item runs on a virtual thread").allMatch(Thread::isVirtual);
        assertThat(new HashSet<>(ranOn.values())).as("each item runs on its own thread").hasSize(3);
    }

    @Test
    void aNullResultStopsThePipeline() throws Exception {
        AtomicInteger later = new AtomicInteger();
        ProcessingPipeline<String> pipeline = new ProcessingPipeline<String>()
                .addStage(x -> x.equals("drop") ? null : x)
                .addStage(x -> { later.incrementAndGet(); return x.toUpperCase(); });
        assertThat(pipeline.execute(s("drop"))).isNull();
        assertThat(later.get()).isZero();
        assertThat(pipeline.execute(s("keep"))).isEqualTo("KEEP");
        assertThat(new ConcurrentPipelineExecutor<>(pipeline).processAll(List.of(s("a"), s("drop"), s("b"))))
                .as("a stopped item keeps its place as null").containsExactly("A", null, "B");
    }

    @Test
    void resultsComeBackInInputOrder() throws Exception {
        AtomicReference<Thread> fastThread = new AtomicReference<>();
        ProcessingPipeline<String> pipeline = new ProcessingPipeline<String>().addStage(x -> {
            if (x.equals("fast")) {
                fastThread.set(Thread.currentThread());
            } else {
                if (!Thread.currentThread().isVirtual()) {
                    throw new IllegalStateException("the slow item is not on a virtual thread");
                }
                // the slow item waits until the fast item's thread has finished its task;
                // the bound only stops a hang when the items do not run at the same time
                long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
                while (fastThread.get() == null) {
                    if (System.nanoTime() > deadline) {
                        throw new IllegalStateException("the items did not run at the same time");
                    }
                    Thread.onSpinWait();
                }
                try {
                    fastThread.get().join();
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            }
            return x.toUpperCase();
        });
        List<String> results = new ConcurrentPipelineExecutor<>(pipeline).processAll(List.of(s("slow"), s("fast")));
        assertThat(results).containsExactly("SLOW", "FAST");
    }
}
