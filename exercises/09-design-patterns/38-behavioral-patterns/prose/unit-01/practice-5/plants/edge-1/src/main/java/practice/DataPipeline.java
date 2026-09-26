package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class DataPipeline<T> {

    private final List<Function<List<T>, List<T>>> stages = new ArrayList<>();

    public DataPipeline<T> addStage(Function<List<T>, List<T>> stage) {
        if (stage == null) {
            throw new IllegalArgumentException("Stage cannot be null");
        }
        stages.add(0, stage);
        return this;
    }

    public List<T> execute(List<T> input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }
        List<T> result = new ArrayList<>(input);
        for (Function<List<T>, List<T>> stage : stages) {
            result = stage.apply(result);
        }
        return result;
    }

    public int stageCount() {
        return stages.size();
    }
}
