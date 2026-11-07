package practice;

import org.junit.jupiter.api.Test;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;

public class PluginHostTest {

    /** A plugin jar as plugins are built: the plugin's classes plus a copy of the host API. */
    private static URL[] pluginJar() {
        return new URL[] {
                PluginHostTest.class.getProtectionDomain().getCodeSource().getLocation(),
                PluginHost.class.getProtectionDomain().getCodeSource().getLocation()};
    }

    public static class GreetingPlugin implements PluginHost.Plugin {
        @Override
        public String run() {
            return "hello from " + getClass().getSimpleName();
        }
    }

    /** Each run makes two calls to the partner API. */
    public static class ShippingPlugin implements PluginHost.Plugin {
        @Override
        public String run() {
            int made = 0;
            for (int i = 0; i < 2; i++) if (PluginHost.Quota.INSTANCE.tryTake()) made++;
            return "calls made: " + made;
        }
    }

    /** Remembers how often it ran, in a static field, as plugins' own singletons do. */
    public static class AuditPlugin implements PluginHost.Plugin {
        private static int runs;

        @Override
        public String run() {
            return "run " + (++runs);
        }
    }

    @Test
    void aPluginRunsThroughTheHost() throws Exception {
        PluginHost.Plugin plugin = PluginHost.load(pluginJar(), "practice.PluginHostTest$GreetingPlugin");
        assertThat(plugin.run()).isEqualTo("hello from GreetingPlugin");
    }

    @Test
    void everyPluginDrawsOnTheHostsQuota() throws Exception {
        int before = PluginHost.Quota.INSTANCE.left();
        PluginHost.Plugin first = PluginHost.load(pluginJar(), "practice.PluginHostTest$ShippingPlugin");
        PluginHost.Plugin second = PluginHost.load(pluginJar(), "practice.PluginHostTest$ShippingPlugin");
        assertThat(first.run()).isEqualTo("calls made: 2");
        assertThat(second.run()).isEqualTo("calls made: 2");
        assertThat(first.run()).isEqualTo("calls made: 2");
        assertThat(PluginHost.Quota.INSTANCE.left()).isEqualTo(before - 6);
    }

    @Test
    void eachPluginKeepsItsOwnStaticState() throws Exception {
        PluginHost.Plugin first = PluginHost.load(pluginJar(), "practice.PluginHostTest$AuditPlugin");
        PluginHost.Plugin second = PluginHost.load(pluginJar(), "practice.PluginHostTest$AuditPlugin");
        assertThat(first.run()).isEqualTo("run 1");
        assertThat(first.run()).isEqualTo("run 2");
        assertThat(second.run()).isEqualTo("run 1");
    }
}
