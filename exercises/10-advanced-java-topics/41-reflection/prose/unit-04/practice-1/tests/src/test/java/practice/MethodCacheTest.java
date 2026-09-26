package practice;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class Vault {
    private int secret() {
        return 4200;
    }
}

class MethodCacheTest {

    static class Target {
        int value() {
            return 7;
        }
    }

    static class Calc {
        int add(int a, int b) {
            return a + b;
        }

        double add(double a, double b) {
            return a + b;
        }
    }

    static class Shop {
        static class Item {
            int price() {
                return 1;
            }
        }
    }

    static class Store {
        static class Item {
            int price() {
                return 2;
            }
        }
    }

    private final AtomicInteger lookups = new AtomicInteger();

    private MethodCache newCache() {
        return new MethodCache((type, name, params) -> {
            lookups.incrementAndGet();
            return type.getDeclaredMethod(name, params);
        });
    }

    @Test
    void looksUpOnceAndReusesTheMethod() throws Exception {
        MethodCache cache = newCache();

        Method first = cache.get(Target.class, "value");
        Method second = cache.get(Target.class, "value");

        assertThat(second).isSameAs(first);
        assertThat(lookups.get()).isEqualTo(1);
        assertThat(cache.size()).isEqualTo(1);
        assertThat(first.invoke(new Target())).isEqualTo(7);
    }

    @Test
    void overloadsAreCachedApart() throws Exception {
        MethodCache cache = newCache();

        Method ints = cache.get(Calc.class, "add", int.class, int.class);
        Method doubles = cache.get(Calc.class, "add", double.class, double.class);

        assertThat(ints.getReturnType()).isSameAs(int.class);
        assertThat(doubles.getReturnType()).isSameAs(double.class);
        assertThat(lookups.get()).isEqualTo(2);
        assertThat(cache.size()).isEqualTo(2);
    }

    @Test
    void classesWithTheSameSimpleNameAreCachedApart() throws Exception {
        MethodCache cache = newCache();

        Method shop = cache.get(Shop.Item.class, "price");
        Method store = cache.get(Store.Item.class, "price");

        assertThat(shop.getDeclaringClass()).isSameAs(Shop.Item.class);
        assertThat(store.getDeclaringClass()).isSameAs(Store.Item.class);
        assertThat(store.invoke(new Store.Item())).isEqualTo(2);
    }

    @Test
    void aCachedPrivateMethodIsReadyToInvoke() throws Exception {
        MethodCache cache = newCache();

        Method secret = cache.get(Vault.class, "secret");

        assertThat(secret.invoke(new Vault())).isEqualTo(4200);
    }
}
