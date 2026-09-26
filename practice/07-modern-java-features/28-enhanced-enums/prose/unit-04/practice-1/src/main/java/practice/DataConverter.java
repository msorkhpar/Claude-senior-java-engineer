package practice;

import java.util.Optional;

public enum DataConverter {
    STRING_TO_NUMBER {
        @Override
        public <T> T convert(Object input, Class<T> targetType) {
            throw new IllegalStateException("write convert");
        }
    },
    TO_STRING {
        @Override
        public <T> T convert(Object input, Class<T> targetType) {
            throw new IllegalStateException("write convert");
        }
    },
    IDENTITY {
        @Override
        @SuppressWarnings("unchecked")
        public <T> T convert(Object input, Class<T> targetType) {
            throw new IllegalStateException("write convert");
        }
    };

    /** Converts {@code input} to {@code targetType}. */
    public abstract <T> T convert(Object input, Class<T> targetType);

    /** The conversion, or empty when it fails for any reason. */
    public <T> Optional<T> safeConvert(Object input, Class<T> targetType) {
        throw new IllegalStateException("write safeConvert");
    }
}
