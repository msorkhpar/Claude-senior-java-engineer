package practice;

import java.util.Objects;
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

        private final String filename;
        private final Function<String, ImageService> loader;
        private volatile ImageService real;

        public VirtualImageProxy(String filename, Function<String, ImageService> loader) {
            this.filename = Objects.requireNonNull(filename, "filename must not be null");
            if (filename.isBlank()) {
                throw new IllegalArgumentException("filename must not be blank");
            }
            this.loader = Objects.requireNonNull(loader, "loader must not be null");
        }

        private ImageService real() {
            ImageService result = real;
            if (result == null) {
                result = loader.apply(filename);
                real = result;
            }
            return result;
        }

        /** Whether the real image has been created yet. */
        public boolean isLoaded() {
            return real != null;
        }

        @Override
        public String display() {
            return real().display();
        }

        @Override
        public String getFilename() {
            return filename;
        }

        @Override
        public long getSize() {
            return real().getSize();
        }
    }
}
