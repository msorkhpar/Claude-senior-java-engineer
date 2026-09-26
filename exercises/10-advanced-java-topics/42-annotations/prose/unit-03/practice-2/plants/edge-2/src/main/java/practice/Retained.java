package practice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public final class Retained {

    private Retained() {
    }

    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.METHOD)
    public @interface ReviewNeeded {
        String reason();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface GeneratedCode {
        String generator();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Cacheable {
        String cacheName() default "default";

        int ttlSeconds() default 300;
    }

    @GeneratedCode(generator = "kit")
    public static class Service {
        @ReviewNeeded(reason = "Optimize SQL query")
        @Cacheable(cacheName = "users", ttlSeconds = 600)
        public List<String> getUsers() {
            return List.of();
        }

        @Cacheable
        public String config() {
            return "production";
        }

        public String plain() {
            return "plain";
        }
    }

    /** "<method> -> <cacheName> (<ttlSeconds>s)" for each @Cacheable method type declares, sorted. */
    public static List<String> cached(Class<?> type) {
        List<String> lines = new ArrayList<>();
        for (Method m : type.getDeclaredMethods()) {
            Cacheable c = m.getAnnotation(Cacheable.class);
            if (c != null) {
                lines.add(m.getName() + " -> " + c.cacheName() + " (" + c.ttlSeconds() + "s)");
            }
        }
        lines.sort(null);
        return lines;
    }
}
