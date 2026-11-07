package practice;

/** The exporters of the reporting service, and the names of the files they write. */
public class Exports {

    /** Writes a report in the service's own format. */
    public static class Exporter {
        String extension() { return "dat"; }

        /**
         * The name of the file this exporter writes for a report: the base name, a dot, and the
         * extension of this exporter's format ("q3" gives "q3.dat" for this class).
         */
        public final String fileName(String base) {
            String ext;
            if (this instanceof CsvExporter) ext = "csv";
            else if (this instanceof GzipCsvExporter) ext = "csv.gz";
            else if (this instanceof JsonExporter) ext = "json";
            else ext = "dat";
            return base + "." + ext;
        }
    }

    public static class CsvExporter extends Exporter {
        @Override String extension() { return "csv"; }
    }

    public static class GzipCsvExporter extends CsvExporter {
        @Override String extension() { return "csv.gz"; }
    }

    public static class JsonExporter extends Exporter {
        @Override String extension() { return "json"; }
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
