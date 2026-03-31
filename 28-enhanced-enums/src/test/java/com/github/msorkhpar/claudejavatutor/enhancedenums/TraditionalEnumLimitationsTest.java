package com.github.msorkhpar.claudejavatutor.enhancedenums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Traditional Enum Limitations Tests")
class TraditionalEnumLimitationsTest {

    @Nested
    @DisplayName("UntypedSetting - No Generic Support")
    class UntypedSettingTest {

        @Test
        @DisplayName("Should return raw Object values requiring unsafe casts")
        void testGetValueReturnsObject() {
            Object value = TraditionalEnumLimitations.UntypedSetting.MAX_RETRIES.getValue();

            assertThat(value).isInstanceOf(Integer.class);
            assertThat((Integer) value).isEqualTo(3);
        }

        @Test
        @DisplayName("Should return String value from Object")
        void testGetStringValue() {
            Object value = TraditionalEnumLimitations.UntypedSetting.APP_NAME.getValue();

            assertThat(value).isInstanceOf(String.class);
            assertThat((String) value).isEqualTo("MyApp");
        }

        @Test
        @DisplayName("Should return Long value requiring cast")
        void testGetLongValue() {
            Object value = TraditionalEnumLimitations.UntypedSetting.TIMEOUT_MS.getValue();

            assertThat(value).isInstanceOf(Long.class);
            assertThat((Long) value).isEqualTo(5000L);
        }

        @Test
        @DisplayName("Should return Boolean value")
        void testGetBooleanValue() {
            Object value = TraditionalEnumLimitations.UntypedSetting.DEBUG_MODE.getValue();

            assertThat(value).isInstanceOf(Boolean.class);
            assertThat((Boolean) value).isTrue();
        }

        @Test
        @DisplayName("Should support getTypedValue with correct type inference")
        void testGetTypedValue() {
            Integer retries = TraditionalEnumLimitations.UntypedSetting.MAX_RETRIES.getTypedValue();
            assertThat(retries).isEqualTo(3);
        }

        @Test
        @DisplayName("Should throw ClassCastException when wrong type used")
        void testGetTypedValueWrongType() {
            assertThatThrownBy(() -> {
                String wrong = TraditionalEnumLimitations.UntypedSetting.MAX_RETRIES.getTypedValue();
                // Force usage to trigger the ClassCastException
                wrong.length();
            }).isInstanceOf(ClassCastException.class);
        }

        @Test
        @DisplayName("Should have correct keys for all settings")
        void testAllKeys() {
            assertThat(TraditionalEnumLimitations.UntypedSetting.MAX_RETRIES.getKey()).isEqualTo("maxRetries");
            assertThat(TraditionalEnumLimitations.UntypedSetting.TIMEOUT_MS.getKey()).isEqualTo("timeout");
            assertThat(TraditionalEnumLimitations.UntypedSetting.APP_NAME.getKey()).isEqualTo("appName");
            assertThat(TraditionalEnumLimitations.UntypedSetting.DEBUG_MODE.getKey()).isEqualTo("debug");
        }
    }

    @Nested
    @DisplayName("OperationWithComposition - Cannot Extend Classes")
    class OperationWithCompositionTest {

        @Test
        @DisplayName("Should perform addition via composition")
        void testAdd() {
            double result = TraditionalEnumLimitations.OperationWithComposition.ADD.apply(3.0, 4.0);
            assertThat(result).isEqualTo(7.0);
        }

        @Test
        @DisplayName("Should perform subtraction")
        void testSubtract() {
            double result = TraditionalEnumLimitations.OperationWithComposition.SUBTRACT.apply(10.0, 3.0);
            assertThat(result).isEqualTo(7.0);
        }

        @Test
        @DisplayName("Should handle negative results")
        void testNegativeResult() {
            double result = TraditionalEnumLimitations.OperationWithComposition.SUBTRACT.apply(3.0, 10.0);
            assertThat(result).isEqualTo(-7.0);
        }

        @Test
        @DisplayName("Should handle zero inputs")
        void testZeroInputs() {
            assertThat(TraditionalEnumLimitations.OperationWithComposition.ADD.apply(0, 0)).isEqualTo(0.0);
            assertThat(TraditionalEnumLimitations.OperationWithComposition.SUBTRACT.apply(0, 0)).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Severity Interface - Fixed Set Workaround")
    class SeverityTest {

        @Test
        @DisplayName("Standard severity should have correct levels")
        void testStandardSeverityLevels() {
            assertThat(TraditionalEnumLimitations.StandardSeverity.LOW.level()).isEqualTo(1);
            assertThat(TraditionalEnumLimitations.StandardSeverity.MEDIUM.level()).isEqualTo(2);
            assertThat(TraditionalEnumLimitations.StandardSeverity.HIGH.level()).isEqualTo(3);
        }

        @Test
        @DisplayName("Extended severity should have higher levels")
        void testExtendedSeverityLevels() {
            assertThat(TraditionalEnumLimitations.ExtendedSeverity.CRITICAL.level()).isEqualTo(4);
            assertThat(TraditionalEnumLimitations.ExtendedSeverity.CATASTROPHIC.level()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should collect all severities from multiple enums sorted by level")
        void testAllSeverities() {
            List<TraditionalEnumLimitations.Severity> all = TraditionalEnumLimitations.allSeverities();

            assertThat(all).hasSize(5);
            assertThat(all.get(0).label()).isEqualTo("Low");
            assertThat(all.get(4).label()).isEqualTo("Catastrophic");
        }

        @Test
        @DisplayName("All severities should be sorted by level ascending")
        void testAllSeveritiesSorted() {
            List<TraditionalEnumLimitations.Severity> all = TraditionalEnumLimitations.allSeverities();

            for (int i = 1; i < all.size(); i++) {
                assertThat(all.get(i).level()).isGreaterThan(all.get(i - 1).level());
            }
        }

        @Test
        @DisplayName("All severities list should be unmodifiable")
        void testAllSeveritiesUnmodifiable() {
            List<TraditionalEnumLimitations.Severity> all = TraditionalEnumLimitations.allSeverities();

            assertThatThrownBy(() -> all.add(TraditionalEnumLimitations.StandardSeverity.LOW))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("DynamicRegistry - Runtime Extensibility")
    class DynamicRegistryTest {

        @Test
        @DisplayName("Should register and retrieve entries")
        void testRegisterAndGet() {
            var registry = new TraditionalEnumLimitations.DynamicRegistry<String>();
            registry.register("greeting", "hello");

            assertThat(registry.get("greeting")).isEqualTo("hello");
        }

        @Test
        @DisplayName("Should throw on duplicate registration")
        void testDuplicateRegistration() {
            var registry = new TraditionalEnumLimitations.DynamicRegistry<String>();
            registry.register("key", "value1");

            assertThatThrownBy(() -> registry.register("key", "value2"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Already registered");
        }

        @Test
        @DisplayName("Should throw on missing entry")
        void testMissingEntry() {
            var registry = new TraditionalEnumLimitations.DynamicRegistry<String>();

            assertThatThrownBy(() -> registry.get("nonexistent"))
                    .isInstanceOf(NoSuchElementException.class)
                    .hasMessageContaining("Not found");
        }

        @Test
        @DisplayName("Should return all values and names")
        void testValuesAndNames() {
            var registry = new TraditionalEnumLimitations.DynamicRegistry<Integer>();
            registry.register("a", 1);
            registry.register("b", 2);

            assertThat(registry.names()).containsExactlyInAnyOrder("a", "b");
            assertThat(registry.values()).containsExactlyInAnyOrder(1, 2);
            assertThat(registry.size()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should handle empty registry")
        void testEmptyRegistry() {
            var registry = new TraditionalEnumLimitations.DynamicRegistry<String>();

            assertThat(registry.size()).isZero();
            assertThat(registry.names()).isEmpty();
            assertThat(registry.values()).isEmpty();
        }
    }

    @Nested
    @DisplayName("DataFormat - Shared Type Constraint")
    class DataFormatTest {

        @Test
        @DisplayName("Should serialize to JSON")
        void testJsonSerialize() {
            String result = TraditionalEnumLimitations.DataFormat.JSON.serialize("hello");
            assertThat(result).isEqualTo("{\"value\":\"hello\"}");
        }

        @Test
        @DisplayName("Should deserialize from JSON")
        void testJsonDeserialize() {
            Object result = TraditionalEnumLimitations.DataFormat.JSON.deserialize("{\"value\":\"hello\"}");
            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("Should serialize and deserialize CSV")
        void testCsvRoundTrip() {
            String serialized = TraditionalEnumLimitations.DataFormat.CSV.serialize(42);
            Object deserialized = TraditionalEnumLimitations.DataFormat.CSV.deserialize(serialized);

            assertThat(deserialized).isEqualTo("42");
        }

        @Test
        @DisplayName("Should handle null objects in CSV serialization")
        void testCsvNull() {
            String result = TraditionalEnumLimitations.DataFormat.CSV.serialize(null);
            assertThat(result).isEqualTo("null");
        }
    }

    @Nested
    @DisplayName("Utility Methods")
    class UtilityMethodsTest {

        @Test
        @DisplayName("Should safely look up enum by name")
        void testSafeValueOf() {
            Optional<TraditionalEnumLimitations.StandardSeverity> result =
                    TraditionalEnumLimitations.safeValueOf(TraditionalEnumLimitations.StandardSeverity.class, "HIGH");

            assertThat(result).contains(TraditionalEnumLimitations.StandardSeverity.HIGH);
        }

        @Test
        @DisplayName("Should return empty for unknown enum name")
        void testSafeValueOfUnknown() {
            Optional<TraditionalEnumLimitations.StandardSeverity> result =
                    TraditionalEnumLimitations.safeValueOf(TraditionalEnumLimitations.StandardSeverity.class, "UNKNOWN");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty for null name")
        void testSafeValueOfNull() {
            Optional<TraditionalEnumLimitations.StandardSeverity> result =
                    TraditionalEnumLimitations.safeValueOf(TraditionalEnumLimitations.StandardSeverity.class, null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should create lookup map from enum")
        void testCreateLookupMap() {
            Map<String, TraditionalEnumLimitations.StandardSeverity> map =
                    TraditionalEnumLimitations.createLookupMap(
                            TraditionalEnumLimitations.StandardSeverity.class,
                            TraditionalEnumLimitations.StandardSeverity::label
                    );

            assertThat(map).hasSize(3);
            assertThat(map.get("Low")).isEqualTo(TraditionalEnumLimitations.StandardSeverity.LOW);
            assertThat(map.get("High")).isEqualTo(TraditionalEnumLimitations.StandardSeverity.HIGH);
        }

        @Test
        @DisplayName("Should create EnumSet from values")
        void testCreateEnumSet() {
            EnumSet<TraditionalEnumLimitations.StandardSeverity> set =
                    TraditionalEnumLimitations.createEnumSet(
                            TraditionalEnumLimitations.StandardSeverity.class,
                            TraditionalEnumLimitations.StandardSeverity.LOW,
                            TraditionalEnumLimitations.StandardSeverity.HIGH
                    );

            assertThat(set).containsExactlyInAnyOrder(
                    TraditionalEnumLimitations.StandardSeverity.LOW,
                    TraditionalEnumLimitations.StandardSeverity.HIGH
            );
        }

        @Test
        @DisplayName("Should create empty EnumSet when no values provided")
        void testCreateEmptyEnumSet() {
            EnumSet<TraditionalEnumLimitations.StandardSeverity> set =
                    TraditionalEnumLimitations.createEnumSet(
                            TraditionalEnumLimitations.StandardSeverity.class
                    );

            assertThat(set).isEmpty();
        }
    }
}
