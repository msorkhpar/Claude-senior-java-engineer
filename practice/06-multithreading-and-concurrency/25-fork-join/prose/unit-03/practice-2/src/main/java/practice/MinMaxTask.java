package practice;

import java.util.concurrent.RecursiveTask;

public final class MinMaxTask extends RecursiveTask<MinMaxTask.Range> {

    public record Range(int min, int max) {
    }

    public MinMaxTask(int[] array, int start, int end, int threshold) {
        throw new UnsupportedOperationException("write the constructor");
    }

    @Override
    protected Range compute() {
        throw new UnsupportedOperationException("write compute");
    }
}
