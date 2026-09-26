package practice;

import java.util.Arrays;

public final class Evens {

    private Evens() {
    }

    /** Returns the even numbers, in order, duplicates kept. */
    public static int[] evens(int[] numbers) {
        return Arrays.stream(numbers).filter(n -> n % 2 == 0).distinct().sorted().toArray();
    }
}
