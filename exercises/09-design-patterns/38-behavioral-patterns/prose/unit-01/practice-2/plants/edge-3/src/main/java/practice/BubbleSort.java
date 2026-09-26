package practice;

import java.util.ArrayList;
import java.util.List;

public final class BubbleSort<T extends Comparable<T>> {

    public List<T> sort(List<T> data) {
        List<T> result = new ArrayList<>(data);
        int n = result.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (result.get(j).compareTo(result.get(j + 1)) > 0) {
                    T temp = result.get(j);
                    result.set(j, result.get(j + 1));
                    result.set(j + 1, temp);
                }
            }
        }
        return result;
    }

    public String name() {
        return "BubbleSort";
    }
}
