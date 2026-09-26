package practice;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
public final class Batch {
    private Batch() {}
    public static <T> List<T> runAll(ForkJoinPool pool, List<? extends ForkJoinTask<T>> tasks) {
        List<ForkJoinTask<T>> pending = new LinkedList<>();
        for (ForkJoinTask<T> t : tasks) pending.add(pool.submit(t));
        List<T> results = new ArrayList<>();
        while (!pending.isEmpty()) {
            for (Iterator<ForkJoinTask<T>> it = pending.iterator(); it.hasNext(); ) {
                ForkJoinTask<T> t = it.next();
                if (t.isDone()) { results.add(t.join()); it.remove(); }
            }
            Thread.onSpinWait();
        }
        return results;
    }
}
