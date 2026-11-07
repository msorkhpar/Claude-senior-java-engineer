package practice;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OperationCacheTest {

    static class ThreadPool {
        int size = 4;

        int resize(int newSize) {
            size = newSize;
            return size;
        }

        String resize(int core, int max) {
            size = max;
            return core + ".." + max;
        }

        private String drain() {
            return "drained " + size;
        }
    }

    static class Billing {
        static class Worker {
            String restart() {
                return "billing worker restarted";
            }
        }
    }

    static class Search {
        static class Worker {
            String restart() {
                return "search worker restarted";
            }
        }
    }

    private final List<String> lookups = new ArrayList<>();

    private OperationCache newCache() {
        return new OperationCache((type, name, params) -> {
            lookups.add(type.getSimpleName() + "." + name);
            return type.getDeclaredMethod(name, params);
        });
    }

    private static Class<?>[] types(Class<?>... types) {
        return types;
    }

    @Test
    void aRepeatedOperationIsLookedUpOnce() throws Exception {
        OperationCache cache = newCache();
        ThreadPool pool = new ThreadPool();

        Object first = cache.run(pool, "resize", types(int.class), 12);
        Object second = cache.run(pool, "resize", types(int.class), 20);
        Object third = cache.run(new ThreadPool(), "resize", types(int.class), 7);

        assertThat(List.of(first, second, third)).containsExactly(12, 20, 7);
        assertThat(lookups).containsExactly("ThreadPool.resize");
        assertThat(cache.size()).isEqualTo(1);
    }

    @Test
    void aPrivateOperationRuns() throws Exception {
        OperationCache cache = newCache();
        ThreadPool pool = new ThreadPool();

        Object first = cache.run(pool, "drain", types());
        Object second = cache.run(pool, "drain", types());

        assertThat(first).isEqualTo("drained 4");
        assertThat(second).isEqualTo("drained 4");
        assertThat(lookups).containsExactly("ThreadPool.drain");
    }

    @Test
    void overloadsAreSeparateOperations() throws Exception {
        OperationCache cache = newCache();
        ThreadPool pool = new ThreadPool();

        Object one = cache.run(pool, "resize", types(int.class), 9);
        Object two = cache.run(pool, "resize", types(int.class, int.class), 2, 16);
        Object again = cache.run(pool, "resize", types(int.class), 3);

        assertThat(one).isEqualTo(9);
        assertThat(two).isEqualTo("2..16");
        assertThat(again).isEqualTo(3);
        assertThat(cache.size()).isEqualTo(2);
    }

    @Test
    void servicesWhoseClassesShareASimpleNameAreKeptApart() throws Exception {
        OperationCache cache = newCache();

        Object billing = cache.run(new Billing.Worker(), "restart", types());
        Object search = cache.run(new Search.Worker(), "restart", types());

        assertThat(billing).isEqualTo("billing worker restarted");
        assertThat(search).isEqualTo("search worker restarted");
        Method m = cache.method(Search.Worker.class, "restart");
        assertThat(m.getDeclaringClass()).isEqualTo(Search.Worker.class);
    }
}
