package practice;

import java.util.Optional;

public final class Directory {

    /** An address; its city may be unknown (null). */
    public record Address(String city) {
    }

    /** A person; their address may be unknown (null). */
    public record Person(String name, Address address) {
    }

    private Directory() {
    }

    /** Returns the city {@code person} lives in, or "Unknown" when any link on the way is null. */
    public static String cityOf(Person person) {
        if (person.address() == null || person.address().city() == null) {
            return "Unknown";
        }
        return person.address().city();
    }
}
