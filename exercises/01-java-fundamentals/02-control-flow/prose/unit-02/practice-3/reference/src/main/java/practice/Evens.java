package practice;

import java.util.Arrays;

public final class Evens {

    private Evens() {
    }

    /** Returns the even numbers, in order, duplicates kept. */
    public static int[] evens(int[] numbers) {
        int count = 0;
        for (int num : numbers) {
            if (num % 2 == 0) {
                count++;
            }
        }
        int[] result = new int[count];
        int index = 0;
        for (int num : numbers) {
            if (num % 2 == 0) {
                result[index++] = num;
            }
        }
        return result;
    }
}
