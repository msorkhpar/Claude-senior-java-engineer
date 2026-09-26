package practice;

import java.lang.reflect.Executable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class DeprecationReport {

    private DeprecationReport() {
    }

    /** One sorted line per deprecated method or constructor the type itself declares. */
    public static List<String> of(Class<?> type) {
        List<Executable> members = new ArrayList<>();
        members.addAll(Arrays.asList(type.getDeclaredConstructors()));
        members.addAll(Arrays.asList(type.getDeclaredMethods()));
        List<String> lines = new ArrayList<>();
        for (Executable member : members) {
            Deprecated d = member.getAnnotation(Deprecated.class);
            if (d == null || member.isSynthetic()) {
                continue;
            }
            String name = member instanceof java.lang.reflect.Constructor<?> ? type.getSimpleName() : member.getName();
            String params = Arrays.stream(member.getParameterTypes())
                    .map(Class::getSimpleName)
                    .collect(Collectors.joining(", "));
            StringBuilder line = new StringBuilder(name).append('(').append(params).append(')');
            line.append(" since ").append(d.since());
            if (d.forRemoval()) {
                line.append(", for removal");
            }
            lines.add(line.toString());
        }
        lines.sort(null);
        return lines;
    }
}
