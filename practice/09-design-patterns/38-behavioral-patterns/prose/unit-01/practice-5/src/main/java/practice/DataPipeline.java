package practice;

import java.util.List;
import java.util.function.Function;

public final class DataPipeline<T> {

    public DataPipeline<T> addStage(Function<List<T>, List<T>> stage) {
        throw new UnsupportedOperationException("TODO");
    }

    public List<T> execute(List<T> input) {
        throw new UnsupportedOperationException("TODO");
    }

    public int stageCount() {
        throw new UnsupportedOperationException("TODO");
    }
}
