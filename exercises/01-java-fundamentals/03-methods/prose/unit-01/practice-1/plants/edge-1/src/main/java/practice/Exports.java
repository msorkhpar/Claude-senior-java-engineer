package practice;

/** The exporters of the reporting service, and the names of the files they write. */
public class Exports {

    /** Writes a report in the service's own format. */
    public static class Exporter {
        static String extension() { return "dat"; }

        /**
         * The name of the file this exporter writes for a report: the base name, a dot, and the
         * extension of this exporter's format ("q3" gives "q3.dat" for this class).
         */
        public final String fileName(String base) {
            return base + "." + extension();
        }
    }

    public static class CsvExporter extends Exporter {
        static String extension() { return "csv"; }
    }

    public static class GzipCsvExporter extends CsvExporter {
        static String extension() { return "csv.gz"; }
    }

    public static class JsonExporter extends Exporter {
        static String extension() { return "json"; }
    }

    /** The exporter for a format the user picked: "csv", "csv.gz", "json", or anything else. */
    public static Exporter forFormat(String format) {
        return switch (format) {
            case "csv" -> new CsvExporter();
            case "csv.gz" -> new GzipCsvExporter();
            case "json" -> new JsonExporter();
            default -> new Exporter();
        };
    }
}
