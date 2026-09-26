package practice;

import java.util.Optional;

public enum DataConverter {
    STRING_TO_NUMBER {
        @Override
        public <T> T convert(Object input, Class<T> targetType) {
            String text = String.valueOf(input);
            Object result;
            if (targetType == Integer.class) {
                result = Integer.parseInt(text);
            } else if (targetType == Long.class) {
                result = (long) Integer.parseInt(text);
            } else if (targetType == Double.class) {
                result = Double.parseDouble(text);
            } else if (targetType == Float.class) {
                result = Float.parseFloat(text);
            } else {
                throw new UnsupportedOperationException("Cannot convert to " + targetType.getSimpleName());
            }
            return targetType.cast(result);
        }
    },
    TO_STRING {
        @Override
        public <T> T convert(Object input, Class<T> targetType) {
            if (targetType != String.class) {
                throw new UnsupportedOperationException("TO_STRING only converts to String");
            }
            return targetType.cast(String.valueOf(input));
        }
    },
    IDENTITY {
        @Override
        @SuppressWarnings("unchecked")
        public <T> T convert(Object input, Class<T> targetType) {
            return targetType.cast(input);
        }
    };

    /** Converts {@code input} to {@code targetType}. */
    public abstract <T> T convert(Object input, Class<T> targetType);

    /** The conversion, or empty when it fails for any reason. */
    public <T> Optional<T> safeConvert(Object input, Class<T> targetType) {
        try {
            return Optional.ofNullable(convert(input, targetType));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }
}
