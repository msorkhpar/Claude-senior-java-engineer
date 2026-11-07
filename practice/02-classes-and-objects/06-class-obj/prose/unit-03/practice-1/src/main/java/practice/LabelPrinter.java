package practice;

/**
 * Renders the address block of a shipping label. The warehouse creates one printer per packing
 * line, and every packer on that line renders labels with it from their own thread.
 */
public class LabelPrinter {

    /** Finds the city for a postcode. It asks a remote service and can take seconds. */
    public interface Cities {
        String cityOf(String postcode);
    }

    private final Cities cities;

    public LabelPrinter(Cities cities) {
        this.cities = cities;
    }

    /** Three lines: the name, the street, and the postcode followed by its city. */
    public String render(String name, String street, String postcode) {
        // TODO: Build the three lines, for example "Ana Silva\n4 Harbour Road\nAB1 2CD Aberdeen",
        //       asking `cities` for the city. Every packer on the line calls this on the same
        //       printer at the same time, and each must get only their own customer's lines.
        //       Count the label for renderedByAll().
        throw new IllegalStateException("TODO: render");
    }

    /** How many labels all printers in this JVM have rendered so far. */
    public static long renderedByAll() {
        // TODO: The warehouse dashboard shows one number for all packing lines: every label any
        //       printer has rendered since the service started.
        throw new IllegalStateException("TODO: renderedByAll");
    }
}
