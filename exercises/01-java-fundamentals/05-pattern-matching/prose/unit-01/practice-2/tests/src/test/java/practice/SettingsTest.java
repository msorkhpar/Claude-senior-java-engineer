package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SettingsTest {

    private static Settings with(Object value) {
        return new Settings(Map.of("allowed-origins", value));
    }

    @Test
    void aListOfStringsIsReturnedInOrder() {
        List<Object> decoded = new ArrayList<>(List.of("https://shop.example.com", "https://admin.example.com"));

        assertThat(with(decoded).strings("allowed-origins"))
                .containsExactly("https://shop.example.com", "https://admin.example.com");
    }

    @Test
    void aStringArrayFromTheCommandLineIsAccepted() {
        String[] fromArgs = {"https://api.example.com"};

        assertThat(with(fromArgs).strings("allowed-origins")).containsExactly("https://api.example.com");
    }

    @Test
    void aSingleNumberIsRefused() {
        assertThatThrownBy(() -> with(8443).strings("allowed-origins"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aListHoldingANumberIsRefusedAtStartup() {
        List<Object> decoded = new ArrayList<>(List.of("https://shop.example.com", 8443));

        assertThatThrownBy(() -> with(decoded).strings("allowed-origins"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anObjectArrayHoldingOnlyStringsIsAccepted() {
        Object[] fromPlugin = {"https://partner.example.com", "https://cdn.example.com"};

        assertThat(with(fromPlugin).strings("allowed-origins"))
                .containsExactly("https://partner.example.com", "https://cdn.example.com");
    }

    @Test
    void anArrayOfPrimitivesIsRefusedWithTheSettingsError() {
        int[] ports = {8443, 9443};

        assertThatThrownBy(() -> with(ports).strings("allowed-origins"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aListHoldingAnEmptyEntryIsRefused() {
        List<Object> decoded = Arrays.asList("https://shop.example.com", null);

        assertThatThrownBy(() -> with(decoded).strings("allowed-origins"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
