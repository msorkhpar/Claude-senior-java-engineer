package practice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

public final class Retained {

    private Retained() {
    }

    // TODO: give each annotation the @Retention its job needs.

    @Target(ElementType.METHOD)
    public @interface ReviewNeeded {
        String reason();
    }

    @Target(ElementType.TYPE)
    public @interface GeneratedCode {
        String generator();
    }

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
        throw new UnsupportedOperationException("TODO");
    }
}
