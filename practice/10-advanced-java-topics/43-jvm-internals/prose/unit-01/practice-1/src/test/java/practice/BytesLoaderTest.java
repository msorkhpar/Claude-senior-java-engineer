package practice;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BytesLoaderTest {

    /** The class every loader defines from bytes. */
    public static class Plugin {
        @Override
        public String toString() {
            return "plugin";
        }
    }

    private static final String NAME = Plugin.class.getName();

    private static Map<String, byte[]> pluginBytes() throws IOException {
        try (InputStream in = BytesLoaderTest.class.getResourceAsStream("BytesLoaderTest$Plugin.class")) {
            return Map.of(NAME, in.readAllBytes());
        }
    }

    @Test
    void definesAClassFromItsBytes() throws Exception {
        BytesLoader loader = new BytesLoader(null, pluginBytes());

        Class<?> loaded = loader.loadClass(NAME);
        Object plugin = loaded.getDeclaredConstructor().newInstance();

        assertThat(loaded.getName()).isEqualTo(NAME);
        assertThat(loaded.getClassLoader()).isSameAs(loader);
        assertThat(loaded).isNotSameAs(Plugin.class);
        assertThat(plugin.toString()).isEqualTo("plugin");
        assertThat(Plugin.class.isInstance(plugin)).isFalse();
        assertThat(loader.getParent()).isNull();
        assertThat(loader.loadClass("java.lang.String")).isSameAs(String.class);

        BytesLoader other = new BytesLoader(null, pluginBytes());
        Class<?> fromOther = other.loadClass(NAME);
        assertThat(fromOther.getName()).isEqualTo(NAME);
        assertThat(fromOther).isNotSameAs(loaded);
        assertThat(fromOther.getClassLoader()).isSameAs(other);
    }

    @Test
    void theParentIsAskedFirst() throws Exception {
        BytesLoader parent = new BytesLoader(null, pluginBytes());
        BytesLoader child = new BytesLoader(parent, pluginBytes());

        Class<?> fromChild = child.loadClass(NAME);

        assertThat(fromChild.getClassLoader()).isSameAs(parent);
    }

    @Test
    void askingTwiceGivesTheSameClass() throws Exception {
        BytesLoader loader = new BytesLoader(null, pluginBytes());

        Class<?> first = loader.loadClass(NAME);
        Class<?> second = loader.loadClass(NAME);

        assertThat(second).isSameAs(first);
    }

    @Test
    void anUnknownNameIsClassNotFound() throws Exception {
        BytesLoader loader = new BytesLoader(null, pluginBytes());

        assertThatThrownBy(() -> loader.loadClass("practice.Missing"))
                .isInstanceOf(ClassNotFoundException.class);
    }
}
