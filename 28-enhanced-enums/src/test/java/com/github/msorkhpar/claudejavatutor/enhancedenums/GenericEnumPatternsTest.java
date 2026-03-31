package com.github.msorkhpar.claudejavatutor.enhancedenums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Generic Enum Patterns Tests")
class GenericEnumPatternsTest {

    @Nested
    @DisplayName("AppSetting - Sealed Interface Pattern")
    class AppSettingTest {

        @Test
        @DisplayName("MaxRetries should return correct typed default")
        void testMaxRetriesDefault() {
            var setting = new GenericEnumPatterns.AppSetting.MaxRetries();

            assertThat(setting.defaultValue()).isEqualTo(3);
            assertThat(setting.key()).isEqualTo("max.retries");
            assertThat(setting.valueType()).isEqualTo(Integer.class);
        }

        @Test
        @DisplayName("AppName should return String type")
        void testAppNameType() {
            var setting = new GenericEnumPatterns.AppSetting.AppName();

            assertThat(setting.defaultValue()).isEqualTo("DefaultApp");
            assertThat(setting.valueType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("DebugMode should return Boolean type")
        void testDebugModeType() {
            var setting = new GenericEnumPatterns.AppSetting.DebugMode();

            assertThat(setting.defaultValue()).isFalse();
            assertThat(setting.valueType()).isEqualTo(Boolean.class);
        }

        @Test
        @DisplayName("TimeoutMs should return Long type")
        void testTimeoutMsType() {
            var setting = new GenericEnumPatterns.AppSetting.TimeoutMs();

            assertThat(setting.defaultValue()).isEqualTo(5000L);
            assertThat(setting.valueType()).isEqualTo(Long.class);
        }

        @Test
        @DisplayName("values() should return all known settings")
        void testValues() {
            List<GenericEnumPatterns.AppSetting<?>> all = GenericEnumPatterns.AppSetting.values();

            assertThat(all).hasSize(4);
        }

        @Test
        @DisplayName("cast should correctly cast matching types")
        void testCastCorrectType() {
            var setting = new GenericEnumPatterns.AppSetting.MaxRetries();

            Integer result = setting.cast(42);

            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("cast should return default for null")
        void testCastNull() {
            var setting = new GenericEnumPatterns.AppSetting.MaxRetries();

            Integer result = setting.cast(null);

            assertThat(result).isEqualTo(3);
        }

        @Test
        @DisplayName("cast should throw ClassCastException for wrong type")
        void testCastWrongType() {
            var setting = new GenericEnumPatterns.AppSetting.MaxRetries();

            assertThatThrownBy(() -> setting.cast("not a number"))
                    .isInstanceOf(ClassCastException.class);
        }
    }

    @Nested
    @DisplayName("TypeSafeConfig - Heterogeneous Container")
    class TypeSafeConfigTest {

        @Test
        @DisplayName("Should store and retrieve typed values")
        void testPutAndGet() {
            var config = new GenericEnumPatterns.TypeSafeConfig();
            var setting = new GenericEnumPatterns.AppSetting.MaxRetries();

            config.put(setting, 10);
            Integer value = config.get(setting);

            assertThat(value).isEqualTo(10);
        }

        @Test
        @DisplayName("Should return default for missing setting")
        void testGetDefault() {
            var config = new GenericEnumPatterns.TypeSafeConfig();
            var setting = new GenericEnumPatterns.AppSetting.AppName();

            String value = config.get(setting);

            assertThat(value).isEqualTo("DefaultApp");
        }

        @Test
        @DisplayName("Should store multiple settings of different types")
        void testMultipleTypes() {
            var config = new GenericEnumPatterns.TypeSafeConfig();
            config.put(new GenericEnumPatterns.AppSetting.MaxRetries(), 5);
            config.put(new GenericEnumPatterns.AppSetting.AppName(), "TestApp");
            config.put(new GenericEnumPatterns.AppSetting.DebugMode(), true);

            assertThat(config.get(new GenericEnumPatterns.AppSetting.MaxRetries())).isEqualTo(5);
            assertThat(config.get(new GenericEnumPatterns.AppSetting.AppName())).isEqualTo("TestApp");
            assertThat(config.get(new GenericEnumPatterns.AppSetting.DebugMode())).isTrue();
            assertThat(config.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should check if setting exists")
        void testContains() {
            var config = new GenericEnumPatterns.TypeSafeConfig();
            var setting = new GenericEnumPatterns.AppSetting.MaxRetries();

            assertThat(config.contains(setting)).isFalse();
            config.put(setting, 5);
            assertThat(config.contains(setting)).isTrue();
        }

        @Test
        @DisplayName("Should throw on null setting")
        void testNullSetting() {
            var config = new GenericEnumPatterns.TypeSafeConfig();

            assertThatThrownBy(() -> config.put(null, 5))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw on null value")
        void testNullValue() {
            var config = new GenericEnumPatterns.TypeSafeConfig();
            var setting = new GenericEnumPatterns.AppSetting.MaxRetries();

            assertThatThrownBy(() -> config.put(setting, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("ConfigKey - Class Token Pattern")
    class ConfigKeyTest {

        @Test
        @DisplayName("Should have correct types for all keys")
        void testConfigKeyTypes() {
            assertThat(GenericEnumPatterns.ConfigKey.MAX_CONNECTIONS.type()).isEqualTo(Integer.class);
            assertThat(GenericEnumPatterns.ConfigKey.SERVER_HOST.type()).isEqualTo(String.class);
            assertThat(GenericEnumPatterns.ConfigKey.ENABLE_SSL.type()).isEqualTo(Boolean.class);
            assertThat(GenericEnumPatterns.ConfigKey.RATE_LIMIT.type()).isEqualTo(Double.class);
        }

        @Test
        @DisplayName("Should return typed defaults")
        void testDefaults() {
            Integer maxConn = GenericEnumPatterns.ConfigKey.MAX_CONNECTIONS.defaultValue();
            String host = GenericEnumPatterns.ConfigKey.SERVER_HOST.defaultValue();

            assertThat(maxConn).isEqualTo(10);
            assertThat(host).isEqualTo("localhost");
        }

        @Test
        @DisplayName("Should cast matching types correctly")
        void testCast() {
            Integer result = GenericEnumPatterns.ConfigKey.MAX_CONNECTIONS.cast(42);
            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("Should return default on null cast")
        void testCastNull() {
            Integer result = GenericEnumPatterns.ConfigKey.MAX_CONNECTIONS.cast(null);
            assertThat(result).isEqualTo(10);
        }

        @Test
        @DisplayName("Should throw on type mismatch cast")
        void testCastMismatch() {
            assertThatThrownBy(() -> GenericEnumPatterns.ConfigKey.MAX_CONNECTIONS.cast("wrong"))
                    .isInstanceOf(ClassCastException.class);
        }

        @Test
        @DisplayName("Should find config key by string key")
        void testFromKey() {
            assertThat(GenericEnumPatterns.ConfigKey.fromKey("max.connections"))
                    .contains(GenericEnumPatterns.ConfigKey.MAX_CONNECTIONS);
            assertThat(GenericEnumPatterns.ConfigKey.fromKey("nonexistent"))
                    .isEmpty();
        }
    }

    @Nested
    @DisplayName("TypedKey - Static Final Instance Pattern")
    class TypedKeyTest {

        @Test
        @DisplayName("Should have correct defaults and names")
        void testDefaults() {
            assertThat(GenericEnumPatterns.TypedKey.PORT.defaultValue()).isEqualTo(8080);
            assertThat(GenericEnumPatterns.TypedKey.HOST.defaultValue()).isEqualTo("0.0.0.0");
            assertThat(GenericEnumPatterns.TypedKey.VERBOSE.defaultValue()).isFalse();
            assertThat(GenericEnumPatterns.TypedKey.ALLOWED_ORIGINS.defaultValue()).containsExactly("*");
        }

        @Test
        @DisplayName("Should list all registered keys")
        void testValues() {
            assertThat(GenericEnumPatterns.TypedKey.values()).hasSizeGreaterThanOrEqualTo(4);
        }

        @Test
        @DisplayName("Should have proper equality based on name")
        void testEquality() {
            assertThat(GenericEnumPatterns.TypedKey.PORT)
                    .isEqualTo(GenericEnumPatterns.TypedKey.PORT);
            assertThat(GenericEnumPatterns.TypedKey.PORT)
                    .isNotEqualTo(GenericEnumPatterns.TypedKey.HOST);
        }

        @Test
        @DisplayName("Should have meaningful toString")
        void testToString() {
            assertThat(GenericEnumPatterns.TypedKey.PORT.toString()).contains("port");
        }

        @Test
        @DisplayName("Should return correct type tokens")
        void testTypeTokens() {
            assertThat(GenericEnumPatterns.TypedKey.PORT.type()).isEqualTo(Integer.class);
            assertThat(GenericEnumPatterns.TypedKey.HOST.type()).isEqualTo(String.class);
            assertThat(GenericEnumPatterns.TypedKey.VERBOSE.type()).isEqualTo(Boolean.class);
        }
    }

    @Nested
    @DisplayName("LazyDefault - Supplier-Based Pattern")
    class LazyDefaultTest {

        @Test
        @DisplayName("Should generate different random ints each time")
        void testRandomInt() {
            Set<Integer> results = new HashSet<>();
            for (int i = 0; i < 50; i++) {
                Integer value = GenericEnumPatterns.LazyDefault.RANDOM_INT.generate();
                results.add(value);
                assertThat(value).isBetween(0, 99);
            }
            // With 50 tries, we should get at least 2 different values
            assertThat(results.size()).isGreaterThan(1);
        }

        @Test
        @DisplayName("Should return current time")
        void testCurrentTime() {
            long before = System.currentTimeMillis();
            Long time = GenericEnumPatterns.LazyDefault.CURRENT_TIME.generate();
            long after = System.currentTimeMillis();

            assertThat(time).isBetween(before, after);
        }

        @Test
        @DisplayName("Should generate new empty list each time")
        void testEmptyList() {
            List<Object> list1 = GenericEnumPatterns.LazyDefault.EMPTY_LIST.generate();
            List<Object> list2 = GenericEnumPatterns.LazyDefault.EMPTY_LIST.generate();

            assertThat(list1).isEmpty();
            assertThat(list2).isEmpty();
            assertThat(list1).isNotSameAs(list2);
        }

        @Test
        @DisplayName("Should generate new empty map each time")
        void testDefaultMap() {
            Map<Object, Object> map1 = GenericEnumPatterns.LazyDefault.DEFAULT_MAP.generate();
            Map<Object, Object> map2 = GenericEnumPatterns.LazyDefault.DEFAULT_MAP.generate();

            assertThat(map1).isEmpty();
            assertThat(map2).isEmpty();
            assertThat(map1).isNotSameAs(map2);
        }

        @Test
        @DisplayName("Should expose supplier")
        void testSupplier() {
            assertThat(GenericEnumPatterns.LazyDefault.EMPTY_LIST.supplier()).isNotNull();
            assertThat(GenericEnumPatterns.LazyDefault.EMPTY_LIST.supplier().get()).isInstanceOf(List.class);
        }
    }
}
