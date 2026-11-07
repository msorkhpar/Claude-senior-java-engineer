package practice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ServiceLoader;

/**
 * The notification service's plug-in point. Each delivery channel ships in a jar of its own,
 * with a class that implements {@link Notifier}, carries {@link Channel}, and is listed in that
 * jar's META-INF/services/practice.Channels$Notifier. Operations choose the channel by name in
 * the service's configuration.
 */
public final class Channels {

    private Channels() {
    }

    /** What the notification service needs from a channel. */
    public interface Notifier {
        /** Delivers the text and returns the channel's receipt for it. */
        String deliver(String recipient, String text);
    }

    /** The name under which a Notifier is chosen in the configuration, such as "sms". */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Channel {
        String value();
    }

    /**
     * Returns the notifier for the configured channel, from the providers that {@code plugins}
     * can see.
     */
    public static Notifier forChannel(String channel, ClassLoader plugins) {
        java.util.Iterator<Notifier> it = ServiceLoader.load(Notifier.class, plugins).iterator();
        while (true) {
            try {
                if (!it.hasNext()) break;
                Notifier candidate = it.next();
                Channel tag = candidate.getClass().getAnnotation(Channel.class);
                if (tag != null && tag.value().equals(channel)) return candidate;
            } catch (java.util.ServiceConfigurationError broken) {
                // a channel that can't start: try the next one
            }
        }
        throw new IllegalArgumentException("no notifier for channel " + channel);
    }
}
