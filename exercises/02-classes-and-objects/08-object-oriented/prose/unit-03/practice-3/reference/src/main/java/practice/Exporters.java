package practice;

import java.util.List;

public class Exporters {

    public abstract static class Exporter {

        /** The whole algorithm: header (if included), one line per item, footer. */
        public final String export(List<String> items) {
            StringBuilder out = new StringBuilder();
            if (includeHeader()) {
                out.append(header()).append('\n');
            }
            for (int position = 0; position < items.size(); position++) {
                out.append(formatItem(position, items.get(position))).append('\n');
            }
            out.append(footer(items.size())).append('\n');
            return out.toString();
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
            return "# Items";
        }

        @Override
        protected String formatItem(int position, String item) {
            return (position + 1) + ". " + item;
        }

        @Override
        protected String footer(int count) {
            return "(" + count + " items)";
        }
    }

    public static class Csv extends Exporter {
        @Override
        protected String header() {
            return "index,item";
        }

        @Override
        protected String formatItem(int position, String item) {
            return position + "," + item;
        }

        @Override
        protected String footer(int count) {
            return "end";
        }
    }

    public static class Plain extends Exporter {
        @Override
        protected String header() {
            return "ITEMS";
        }

        @Override
        protected String formatItem(int position, String item) {
            return item;
        }

        @Override
        protected String footer(int count) {
            return "--";
        }

        @Override
        protected boolean includeHeader() {
            return false;
        }
    }
}
