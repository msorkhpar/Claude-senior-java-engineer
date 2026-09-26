package practice;

import java.util.List;
import java.util.Optional;

public final class Settings {

    private Settings() {
    }

    /** A setting whose value has the type T. */
    public sealed interface AppSetting<T> permits MaxRetries, AppName, DebugMode, TimeoutMs {
        String key();

        T defaultValue();

        Class<T> valueType();
    }

    public record MaxRetries() implements AppSetting<Integer> {
        public String key() {
            return "max.retries";
        }

        public Integer defaultValue() {
            return 3;
        }

        public Class<Integer> valueType() {
            return Integer.class;
        }
    }

    public record AppName() implements AppSetting<String> {
        public String key() {
            return "app.name";
        }

        public String defaultValue() {
            return "DefaultApp";
        }

        public Class<String> valueType() {
            return String.class;
        }
    }

    public record DebugMode() implements AppSetting<Boolean> {
        public String key() {
            return "debug.mode";
        }

        public Boolean defaultValue() {
            return false;
        }

        public Class<Boolean> valueType() {
            return Boolean.class;
        }
    }

    public record TimeoutMs() implements AppSetting<Long> {
        public String key() {
            return "timeout.ms";
        }

        public Long defaultValue() {
            return 5000L;
        }

        public Class<Long> valueType() {
            return Long.class;
        }
    }

    /** One instance of each setting, in declaration order. */
    public static List<AppSetting<?>> values() {
        return List.of(new MaxRetries(), new AppName(), new DebugMode(), new TimeoutMs());
    }

    /** The setting whose key equals {@code key}, or empty. */
    public static Optional<AppSetting<?>> byKey(String key) {
        return values().stream().filter(s -> s.key().equals(key)).findFirst();
    }

    /** The value of {@code setting} read from {@code raw}; null gives the default. */
    public static <T> T parse(AppSetting<T> setting, String raw) {
        if (raw == null) {
            return setting.defaultValue();
        }
        String text = raw.strip();
        AppSetting<?> any = setting;
        Object value = switch (any) {
            case MaxRetries m -> Integer.valueOf(text);
            case AppName n -> text;
            case DebugMode d -> Boolean.valueOf(text);
            case TimeoutMs t -> Long.valueOf(text);
        };
        return setting.valueType().cast(value);
    }
}
