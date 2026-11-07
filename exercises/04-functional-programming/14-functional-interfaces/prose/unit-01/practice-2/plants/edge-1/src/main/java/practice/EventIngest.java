package practice;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Takes the batches of event ids that devices upload. Every id received goes to the raw archive,
 * repeats included, because the archive is the record of what the devices sent. Subscribers are
 * told about an event once: the first time its id arrives, in this batch or any earlier one.
 */
public class EventIngest {

    private final Set<String> seen = new HashSet<>();
    private final Consumer<String> archive;
    private final Consumer<String> subscribers;

    /**
     * @param archive     appends one id to the raw archive
     * @param subscribers tells the subscribers about one new event
     */
    public EventIngest(Consumer<String> archive, Consumer<String> subscribers) {
        this.archive = archive;
        this.subscribers = subscribers;
    }

    /** Takes one uploaded batch and returns how many ids it held. */
    public long ingest(List<String> batch) {
        batch.stream().filter(seen::add).forEach(subscribers);
        return batch.stream().peek(archive).count();
    }
}
