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

        public EncryptionDecorator(DataSource wrappee, int shift) {
        }

        @Override
        public String read() {
            throw new UnsupportedOperationException("write read");
        }

        @Override
        public void write(String data) {
            throw new UnsupportedOperationException("write write");
        }
    }

    public static final class CompressionDecorator implements DataSource {

        public CompressionDecorator(DataSource wrappee) {
        }

        @Override
        public String read() {
            throw new UnsupportedOperationException("write read");
        }

        @Override
        public void write(String data) {
            throw new UnsupportedOperationException("write write");
        }
    }
}
