package practice;

import java.util.function.Function;

public final class Images {

    private Images() {
    }

    public interface ImageService {
        String display();

        String getFilename();

        long getSize();
    }

    public static final class VirtualImageProxy implements ImageService {

        public VirtualImageProxy(String filename, Function<String, ImageService> loader) {
            throw new UnsupportedOperationException("write the constructor");
        }

        /** Whether the real image has been created yet. */
        public boolean isLoaded() {
            throw new UnsupportedOperationException("write isLoaded");
        }

        @Override
        public String display() {
            throw new UnsupportedOperationException("write display");
        }

        @Override
        public String getFilename() {
            throw new UnsupportedOperationException("write getFilename");
        }

        @Override
        public long getSize() {
            throw new UnsupportedOperationException("write getSize");
        }
    }
}
