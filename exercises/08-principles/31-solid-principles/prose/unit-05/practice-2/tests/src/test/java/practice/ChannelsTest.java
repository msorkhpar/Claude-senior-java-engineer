package practice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class ChannelsTest {

    // ---- Three channels, as their jars would ship them. ----

    @Channels.Channel("sms")
    public static class Twilio implements Channels.Notifier {
        static final AtomicInteger started = new AtomicInteger();

        public Twilio() {
            started.incrementAndGet();
        }

        public String deliver(String recipient, String text) {
            return "sms:" + recipient + ":" + text;
        }
    }

    @Channels.Channel("email")
    public static class Postfix implements Channels.Notifier {
        static final AtomicInteger started = new AtomicInteger();

        public Postfix() {
            started.incrementAndGet();
        }

        public String deliver(String recipient, String text) {
            return "email:" + recipient + ":" + text;
        }
    }

    /** Needs a key the test machine doesn't have. */
    @Channels.Channel("push")
    public static class Firebase implements Channels.Notifier {
        public Firebase() {
            throw new IllegalStateException("PUSH_KEY is not set");
        }

        public String deliver(String recipient, String text) {
            return "push:" + recipient + ":" + text;
        }
    }

    @TempDir
    Path jarRoot;

    @BeforeEach
    void resetCounts() {
        Twilio.started.set(0);
        Postfix.started.set(0);
    }

    /** A class loader that sees one plug-in directory listing the given providers, in that order. */
    private ClassLoader plugins(Class<?>... providers) throws IOException {
        Path services = jarRoot.resolve("META-INF/services");
        Files.createDirectories(services);
        StringBuilder listing = new StringBuilder("# channels shipped with this build\n");
        for (Class<?> provider : providers) listing.append(provider.getName()).append('\n');
        Files.writeString(services.resolve(Channels.Notifier.class.getName()), listing);
        return new URLClassLoader(new URL[] {jarRoot.toUri().toURL()}, ChannelsTest.class.getClassLoader());
    }

    /**
     * Runs the call on a fresh daemon thread whose context class loader is {@code context}, and
     * gives up after ten seconds. Returns the result, or the exception or error it threw.
     */
    private static Object call(ClassLoader context, Supplier<Object> body) throws Exception {
        CompletableFuture<Object> outcome = new CompletableFuture<>();
        Thread t = new Thread(() -> {
            try {
                outcome.complete(body.get());
            } catch (Throwable e) {
                outcome.complete(e);
            }
        });
        t.setDaemon(true);
        t.setContextClassLoader(context);
        t.start();
        return outcome.get(10, TimeUnit.SECONDS);
    }

    private static ClassLoader application() {
        return ChannelsTest.class.getClassLoader();
    }

    @Test
    void deliversThroughTheConfiguredChannel() throws Exception {
        ClassLoader plugins = plugins(Twilio.class, Postfix.class);

        Object email = call(plugins, () -> Channels.forChannel("email", plugins));
        Object sms = call(plugins, () -> Channels.forChannel("sms", plugins));

        assertThat(email).isInstanceOf(Postfix.class);
        assertThat(((Channels.Notifier) email).deliver("ops@example.com", "disk 91% full"))
                .isEqualTo("email:ops@example.com:disk 91% full");
        assertThat(sms).isInstanceOf(Twilio.class);
    }

    @Test
    void looksForChannelsOnlyWhereItIsTold() throws Exception {
        ClassLoader plugins = plugins(Twilio.class, Postfix.class);

        Object sms = call(application(), () -> Channels.forChannel("sms", plugins));

        assertThat(sms).isInstanceOf(Twilio.class);
    }

    @Test
    void aChannelThatCannotStartLeavesTheOthersWorking() throws Exception {
        ClassLoader plugins = plugins(Firebase.class, Twilio.class);

        Object sms = call(plugins, () -> Channels.forChannel("sms", plugins));

        assertThat(sms).isInstanceOf(Twilio.class);
    }

    @Test
    void onlyTheChosenChannelIsStarted() throws Exception {
        ClassLoader plugins = plugins(Twilio.class, Postfix.class);

        Object email = call(plugins, () -> Channels.forChannel("email", plugins));

        assertThat(email).isInstanceOf(Postfix.class);
        assertThat(Twilio.started).hasValue(0);
        assertThat(Postfix.started).hasValue(1);
    }

    @Test
    void aChannelNobodyShippedIsRefusedByName() throws Exception {
        ClassLoader plugins = plugins(Twilio.class, Postfix.class);

        Object fax = call(plugins, () -> Channels.forChannel("fax", plugins));

        assertThat(fax).isInstanceOf(IllegalArgumentException.class);
        assertThat(((Throwable) fax).getMessage()).contains("fax");
    }
}
