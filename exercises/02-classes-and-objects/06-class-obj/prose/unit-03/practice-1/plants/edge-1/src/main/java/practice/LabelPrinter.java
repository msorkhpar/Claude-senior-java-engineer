package practice;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Renders the address block of a shipping label. The warehouse creates one printer per packing
 * line, and every packer on that line renders labels with it from their own thread.
 */
public class LabelPrinter {

    /** Finds the city for a postcode. It asks a remote service and can take seconds. */
    public interface Cities {
        String cityOf(String postcode);
    }

    private static final AtomicLong RENDERED = new AtomicLong();
    private static final StringBuilder SCRATCH = new StringBuilder(96);   // reused to save allocations

    private final Cities cities;

    public LabelPrinter(Cities cities) {
        this.cities = cities;
    }

    /** Three lines: the name, the street, and the postcode followed by its city. */
    public String render(String name, String street, String postcode) {
        StringBuilder label = SCRATCH;
        label.setLength(0);
        label.append(name).append('\n')
                .append(street).append('\n')
                .append(postcode).append(' ').append(cities.cityOf(postcode));
        RENDERED.incrementAndGet();
        return label.toString();
    }

    /** How many labels all printers in this JVM have rendered so far. */
    public static long renderedByAll() {
        return RENDERED.get();
    }
}
