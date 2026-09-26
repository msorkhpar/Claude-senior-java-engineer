package practice;

import java.util.Arrays;

public final class Ranking {

    private Ranking() {
    }

    /** Returns a new array of the scores from highest to lowest. */
    public static int[] ranked(int[] scores) {
        for (int i = 1; i < scores.length; i++) {
            if (scores[i - 1] < scores[i]) {
                int[] copy = Arrays.copyOf(scores, scores.length);
                Arrays.sort(copy);
                for (int left = 0, right = copy.length - 1; left < right; left++, right--) {
                    int temp = copy[left];
                    copy[left] = copy[right];
                    copy[right] = temp;
                }
                return copy;
            }
        }
        return scores;
    }
}
