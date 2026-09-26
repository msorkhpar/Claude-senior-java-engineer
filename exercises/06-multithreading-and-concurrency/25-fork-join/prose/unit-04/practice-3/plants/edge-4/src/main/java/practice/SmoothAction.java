package practice;
import java.util.concurrent.RecursiveAction;
public final class SmoothAction extends RecursiveAction {
    private final int[] src, dst; private final int s, e, t;
    public SmoothAction(int[] source, int[] dest, int start, int end, int threshold) { src = source; dst = dest; s = start; e = end; t = threshold; }
    @Override protected void compute() {
        if (e - s <= t) {
            int n = src.length;
            for (int i = s; i < e; i++) {
                if (i == 0 || i == n - 1) dst[i] = src[i];
                else dst[i] = Math.floorDiv(src[i - 1] + src[i] + src[i + 1], 3);
            }
            return;
        }
        int mid = s + (e - s) / 2;
        invokeAll(new SmoothAction(src, dst, s, mid, t), new SmoothAction(src, dst, mid, e, t));
    }
}
