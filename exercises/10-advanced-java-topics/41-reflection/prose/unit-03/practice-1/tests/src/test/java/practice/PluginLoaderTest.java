package practice;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PluginLoaderTest {

    static class DefaultPlugin implements PluginLoader.Plugin {
        @Override
        public String execute() {
            return "Default executed";
        }
    }

    static class HiddenPlugin implements PluginLoader.Plugin {
        private HiddenPlugin() {
        }

        @Override
        public String execute() {
            return "Hidden executed";
        }
    }

    static class NotAPlugin {
        static final AtomicInteger MADE = new AtomicInteger();

        NotAPlugin() {
            MADE.incrementAndGet();
        }
    }

    abstract static class AbstractPlugin implements PluginLoader.Plugin {
    }

    @Test
    void loadsAPluginByName() {
        PluginLoader.Plugin first = PluginLoader.load("practice.PluginLoaderTest$DefaultPlugin");
        PluginLoader.Plugin second = PluginLoader.load("practice.PluginLoaderTest$DefaultPlugin");

        assertThat(first.execute()).isEqualTo("Default executed");
        assertThat(second).isInstanceOf(DefaultPlugin.class).isNotSameAs(first);
        assertThatThrownBy(() -> PluginLoader.load("practice.NoSuchPlugin"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aPrivateConstructorStillLoads() {
        assertThat(PluginLoader.load("practice.PluginLoaderTest$HiddenPlugin").execute())
                .isEqualTo("Hidden executed");
    }

    @Test
    void aClassThatIsNotAPluginIsRefusedBeforeCreation() {
        int before = NotAPlugin.MADE.get();

        assertThatThrownBy(() -> PluginLoader.load("practice.PluginLoaderTest$NotAPlugin"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(NotAPlugin.MADE.get()).isEqualTo(before);
    }

    @Test
    void anAbstractPluginIsRefused() {
        assertThatThrownBy(() -> PluginLoader.load("practice.PluginLoaderTest$AbstractPlugin"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PluginLoader.load("practice.PluginLoader$Plugin"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
