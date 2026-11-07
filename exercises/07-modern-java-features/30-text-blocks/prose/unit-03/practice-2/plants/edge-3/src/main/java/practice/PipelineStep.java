package practice;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Generates one step of the CI pipeline file from a shell script kept in the repository's ci/
 * directory. The generated file is checked in, compared byte for byte with the previous one, and
 * linted before every merge.
 */
public class PipelineStep {

    /** Renders a step that runs the script as a YAML literal block. */
    public static String render(String name, String script) {
        return """
                  - name: %s
                    run: |
                %s""".formatted(name, runBlock(script));
    }

    /**
     * The script as the body of the step's run: block. The script is a file's content: it may or
     * may not end with a line break, and it was written on whatever machine its author uses.
     */
    static String runBlock(String script) {
        String[] lines = script.stripTrailing().split("\n");
        int margin = Arrays.stream(lines).filter(line -> !line.isBlank())
                .mapToInt(line -> line.length() - line.stripLeading().length()).min().orElse(0);
        return Arrays.stream(lines)
                .map(line -> line.isBlank() ? "" : "      " + line.substring(margin))
                .collect(Collectors.joining("\n", "", "\n"));
    }
}
