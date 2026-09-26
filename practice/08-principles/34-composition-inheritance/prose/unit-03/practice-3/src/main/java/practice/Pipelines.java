package practice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

/** A pipeline of stages, and an executor that runs many items through it at once. */
public final class Pipelines {

    private Pipelines() {
    }

    public static final class ProcessingPipeline<T> {
        private final List<Function<T, T>> stages = new ArrayList<>();

        public ProcessingPipeline<T> addStage(Function<T, T> stage) {
            stages.add(Objects.requireNonNull(stage, "stage"));
            return this;
        }

        /** Runs input through every stage in order; a stage that returns null stops the pipeline. */
        public T execute(T input) {
            throw new UnsupportedOperationException("write execute");
        }
    }

    public static final class ConcurrentPipelineExecutor<T> {
        private final ProcessingPipeline<T> pipeline;

        public ConcurrentPipelineExecutor(ProcessingPipeline<T> pipeline) {
            this.pipeline = Objects.requireNonNull(pipeline, "pipeline");
        }

        /** Runs each item through the pipeline on its own virtual thread; results in input order. */
        public List<T> processAll(List<T> items) throws InterruptedException, ExecutionException {
            throw new UnsupportedOperationException("write processAll");
        }
    }
}
