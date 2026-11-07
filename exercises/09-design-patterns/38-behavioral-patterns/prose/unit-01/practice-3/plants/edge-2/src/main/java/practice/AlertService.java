package practice;

import java.util.Map;
import java.util.TreeSet;

/** Sends operational alerts through the channel that config names under "alerts.channel". */
public class AlertService {

    public interface Channel {
        void deliver(String message);
    }

    static final String KEY = "alerts.channel";
    static final String DEFAULT_CHANNEL = "email";

    private final Map<String, Channel> channels;
    private final Channel channel;

    /**
     * @param channels every channel this build can send through, by the name config uses for it;
     *                 it always includes "email"
     * @param config   the service's settings, read once at start-up
     */
    public AlertService(Map<String, Channel> channels, Map<String, String> config) {
        this.channels = Map.copyOf(channels);
        this.channel = choose(config);
    }

    /** Picks the channel every alert of this service goes through. */
    Channel choose(Map<String, String> config) {
        String name = config.getOrDefault(KEY, DEFAULT_CHANNEL);
        return channels.getOrDefault(name, channels.get(DEFAULT_CHANNEL));
    }

    public void raise(String message) {
        channel.deliver(message);
    }
}
