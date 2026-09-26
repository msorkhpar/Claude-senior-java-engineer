package practice;

import java.util.Arrays;
import java.util.List;

public final class UniqueWords {

    private UniqueWords() {
    }

    /** Returns the distinct words of the input, sorted. */
    public static List<String> of(String input) {
        if (input == null || input.isBlank()) {
            return List.of();
        }
        return Arrays.stream(input.strip().split("\\s+"))
                .sorted()
                .toList();
    }
}
