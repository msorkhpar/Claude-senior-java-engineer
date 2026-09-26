package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/** Text transformations composed at run time, in the order they are added. */
public class TextProcessor {
    private final List<Function<String, String>> transformations = new ArrayList<>();

    /** Adds one transformation after the ones already added, and returns this processor. */
    public TextProcessor addTransformation(Function<String, String> transformation) {
        transformations.add(Objects.requireNonNull(transformation, "transformation"));
        return this;
    }

    /** Applies every transformation added so far, in the order they were added. */
    public String process(String input) {
        String result = input;
        for (Function<String, String> t : transformations) {
            result = t.apply(result);
        }
        return result;
    }
}
