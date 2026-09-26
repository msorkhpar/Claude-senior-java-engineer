package practice;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeyedInjectorTest {

    static class Named {
        @KeyedInjector.Inject("dataSource")
        private String dataSource;

        @KeyedInjector.Inject("pool")
        private Integer poolSize;
    }

    static class InjectableService {
        @KeyedInjector.Inject("dataSource")
        private String dataSource;

        @KeyedInjector.Inject
        private String config;
    }

    static class WithDefault {
        @KeyedInjector.Inject("dataSource")
        private String dataSource;

        @KeyedInjector.Inject
        private String region = "eu";
    }

    static class WithPlainField {
        @KeyedInjector.Inject("dataSource")
        private String dataSource;

        private String secret = "kept";
    }

    private static Map<String, Object> registry(Object... pairs) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((String) pairs[i], pairs[i + 1]);
        }
        return map;
    }

    @Test
    void injectsPrivateFieldsByTheirNamedKey() throws Exception {
        Named target = new Named();
        Integer size = Integer.valueOf(1000 + target.hashCode() % 7);

        KeyedInjector.inject(target, registry("dataSource", "jdbc:mysql://localhost/db", "pool", size,
                "poolSize", Integer.valueOf(1)));

        assertThat(target.dataSource).isEqualTo("jdbc:mysql://localhost/db");
        assertThat(target.poolSize).isSameAs(size);
    }

    @Test
    void aBareInjectUsesTheFieldName() throws Exception {
        InjectableService svc = new InjectableService();

        KeyedInjector.inject(svc, registry("dataSource", "jdbc:mysql://localhost/db", "config", "production"));

        assertThat(svc.dataSource).isEqualTo("jdbc:mysql://localhost/db");
        assertThat(svc.config).isEqualTo("production");
    }

    @Test
    void aMissingKeyKeepsTheFieldsValue() throws Exception {
        WithDefault target = new WithDefault();

        KeyedInjector.inject(target, registry("dataSource", "jdbc:h2:mem:orders"));

        assertThat(target.dataSource).isEqualTo("jdbc:h2:mem:orders");
        assertThat(target.region).isEqualTo("eu");

        WithDefault again = new WithDefault();
        KeyedInjector.inject(again, registry("dataSource", "jdbc:h2:mem:orders", "region", null));
        assertThat(again.region).isEqualTo("eu");
    }

    @Test
    void aFieldWithoutInjectIsNeverTouched() throws Exception {
        WithPlainField target = new WithPlainField();

        KeyedInjector.inject(target, registry("dataSource", "jdbc:h2:mem:orders", "secret", "leaked"));

        assertThat(target.dataSource).isEqualTo("jdbc:h2:mem:orders");
        assertThat(target.secret).isEqualTo("kept");
    }
}
