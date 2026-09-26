package practice;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Words {

    private Words() {
    }

    /** Returns every word of every non-null sentence, in order. */
    public static List<String> words(List<String> sentences) {
        return sentences.stream()
                .filter(Objects::nonNull)
                .flatMap(sentence -> Arrays.stream(sentence.split(" ")))
                .toList();
    }

    /** Returns the values of the present optionals, in order. */
    public static List<String> present(List<Optional<String>> values) {
        return values.stream()
                .flatMap(Optional::stream)
                .toList();
    }
}
