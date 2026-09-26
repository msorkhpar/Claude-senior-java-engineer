package practice;

import java.util.Map;
import java.util.stream.Collectors;

public final class JavaClass {

    private JavaClass() {
    }

    /** Returns the source of a class {@code name} in {@code pkg} with {@code fields} (name to type). */
    public static String source(String pkg, String name, Map<String, String> fields) {
        String block = fields.isEmpty() ? "" : fields.entrySet().stream()
                .map(field -> "private %s %s;".formatted(field.getValue(), field.getKey()))
                .collect(Collectors.joining("\n"))
                .indent(4) + "\n";
        return """
                package %s;

                public class %s {

                %s    public %s() {
                    }
                }
                """.formatted(pkg, name, block, name);
    }
}
