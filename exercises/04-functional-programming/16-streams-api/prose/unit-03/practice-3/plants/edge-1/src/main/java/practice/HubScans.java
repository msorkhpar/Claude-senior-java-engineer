package practice;

import java.util.List;
import java.util.Objects;

/**
 * The sorting hub's scanners report every parcel that passes them, one line per scan:
 * "carrier;tracking;time". A parcel that is scanned twice, by two scanners or after a jam, is
 * still one parcel, and the loading list must hold it once.
 */
public class HubScans {

    /** One scan of a parcel. The carrier and the tracking number identify the parcel. */
    public static final class Parcel {
        private final String carrier;
        private final String tracking;
        private final String scannedAt;

        public Parcel(String carrier, String tracking, String scannedAt) {
            this.carrier = carrier;
            this.tracking = tracking;
            this.scannedAt = scannedAt;
        }

        public String carrier() { return carrier; }
        public String tracking() { return tracking; }
        public String scannedAt() { return scannedAt; }

        @Override
        public boolean equals(Object o) {
            return o instanceof Parcel p && carrier == p.carrier && tracking == p.tracking;
        }

        @Override
        public int hashCode() {
            return Objects.hash(carrier, tracking);
        }

        @Override
        public String toString() {
            return carrier + ";" + tracking + ";" + scannedAt;
        }
    }

    /** Reads one scanner line, such as "DHL;JD0142;08:15". */
    public static Parcel parse(String line) {
        String[] f = line.split(";");
        return new Parcel(f[0], f[1], f[2]);
    }

    /** The loading list: every scanned parcel once, in the order it was first scanned. */
    public static List<Parcel> loadingList(List<String> scannerLines) {
        return unique(scannerLines.stream().map(HubScans::parse).toList());
    }

    /** Each parcel once, as first scanned, in the order of the scans. */
    static List<Parcel> unique(List<Parcel> scans) {
        return scans.stream().distinct().toList();
    }
}
