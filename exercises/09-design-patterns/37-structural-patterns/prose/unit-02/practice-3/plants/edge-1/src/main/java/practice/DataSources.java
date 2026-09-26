package practice;

import java.util.Objects;

public final class DataSources {

    private DataSources() {
    }

    public interface DataSource {
        String read();

        void write(String data);
    }

    /** The concrete component: keeps the last text written. */
    public static final class InMemoryDataSource implements DataSource {
        private String data = "";

        @Override
        public String read() {
            return data;
        }

        @Override
        public void write(String data) {
            this.data = Objects.requireNonNull(data, "data must not be null");
        }
    }

    public static final class EncryptionDecorator implements DataSource {

        private final DataSource wrappee;
        private final int shift;

        public EncryptionDecorator(DataSource wrappee, int shift) {
            this.wrappee = Objects.requireNonNull(wrappee, "data source must not be null");
            this.shift = shift;
        }

        @Override
        public String read() {
            return shifted(wrappee.read(), -shift);
        }

        @Override
        public void write(String data) {
            Objects.requireNonNull(data, "data must not be null");
            wrappee.write(shifted(data, shift));
        }

        private static String shifted(String text, int by) {
            StringBuilder sb = new StringBuilder(text.length());
            for (int i = 0; i < text.length(); i++) {
                sb.append((char) (text.charAt(i) + by));
            }
            return sb.toString();
        }
    }

    public static final class CompressionDecorator implements DataSource {

        private final DataSource wrappee;

        public CompressionDecorator(DataSource wrappee) {
            this.wrappee = Objects.requireNonNull(wrappee, "data source must not be null");
        }

        @Override
        public String read() {
            return decompress(wrappee.read());
        }

        @Override
        public void write(String data) {
            Objects.requireNonNull(data, "data must not be null");
            wrappee.write(compress(data));
        }

        private static String compress(String data) {
            if (data.chars().anyMatch(Character::isDigit)) {
                throw new IllegalArgumentException("run-length encoding cannot store digits");
            }
            StringBuilder sb = new StringBuilder();
            int i = 0;
            while (i < data.length()) {
                char c = data.charAt(i);
                int run = 1;
                while (i + run < data.length() && data.charAt(i + run) == c) {
                    run++;
                }
                sb.append(c).append(run);
                i += run;
            }
            return sb.toString();
        }

        private static String decompress(String data) {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            while (i < data.length()) {
                char c = data.charAt(i++);
                int start = i;
                while (i < data.length() && Character.isDigit(data.charAt(i))) {
                    i++;
                }
                int run = start == i ? 1 : Integer.parseInt(data.substring(start, i));
                sb.append(String.valueOf(c).repeat(run));
            }
            return sb.toString();
        }
    }
}
