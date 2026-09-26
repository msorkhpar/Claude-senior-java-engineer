package practice;

import java.util.List;

public class Exporters {

    public abstract static class Exporter {

        /** The whole algorithm: header (if included), one line per item, footer. */
        public final String export(List<String> items) {
            throw new UnsupportedOperationException("write export");
        }

        protected abstract String header();

        protected abstract String formatItem(int position, String item);

        protected abstract String footer(int count);

        /** A hook: whether export writes the header line. */
        protected boolean includeHeader() {
            return true;
        }
    }

    public static class Markdown extends Exporter {
        @Override
        protected String header() {
            throw new UnsupportedOperationException("write Markdown.header");
        }

        @Override
        protected String formatItem(int position, String item) {
            throw new UnsupportedOperationException("write Markdown.formatItem");
        }

        @Override
        protected String footer(int count) {
            throw new UnsupportedOperationException("write Markdown.footer");
        }
    }

    public static class Csv extends Exporter {
        @Override
        protected String header() {
            throw new UnsupportedOperationException("write Csv.header");
        }

        @Override
        protected String formatItem(int position, String item) {
            throw new UnsupportedOperationException("write Csv.formatItem");
        }

        @Override
        protected String footer(int count) {
            throw new UnsupportedOperationException("write Csv.footer");
        }
    }

    public static class Plain extends Exporter {
        @Override
        protected String header() {
            throw new UnsupportedOperationException("write Plain.header");
        }

        @Override
        protected String formatItem(int position, String item) {
            throw new UnsupportedOperationException("write Plain.formatItem");
        }

        @Override
        protected String footer(int count) {
            throw new UnsupportedOperationException("write Plain.footer");
        }
    }
}
