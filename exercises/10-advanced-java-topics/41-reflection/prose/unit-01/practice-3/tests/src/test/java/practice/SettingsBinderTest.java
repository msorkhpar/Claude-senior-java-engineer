package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SettingsBinderTest {

    static class MailSettings {
        List<String> relays = new ArrayList<>();
    }

    static class RetrySettings {
        List<Long> backoffMillis = new ArrayList<>(List.of(100L, 200L));
    }

    static class GatewaySettings {
        List<Integer> listenPorts = new ArrayList<>();
    }

    static class PortList extends ArrayList<Integer> {}

    static class ProxySettings {
        PortList upstreamPorts = new PortList();
    }

    @Test
    void textItemsAreBoundIntoAListOfStrings() {
        MailSettings s = new MailSettings();

        SettingsBinder.bind(s, Map.of("relays", "smtp1.example.com, smtp2.example.com"));

        assertThat(s.relays).containsExactly("smtp1.example.com", "smtp2.example.com");
    }

    @Test
    void numbersReplaceTheDefaultsOfAListOfLongs() {
        RetrySettings s = new RetrySettings();

        SettingsBinder.bind(s, Map.of("backoffMillis", "250,1000,4000"));

        assertThat(s.backoffMillis).containsExactly(250L, 1000L, 4000L);
    }

    @Test
    void numbersAreBoundIntoAnEmptyListOfIntegers() {
        GatewaySettings s = new GatewaySettings();

        SettingsBinder.bind(s, Map.of("listenPorts", "8443,9443"));

        assertThat(s.listenPorts).containsExactly(8443, 9443);
    }

    @Test
    void numbersAreBoundIntoAFieldWhoseClassExtendsAListOfIntegers() {
        ProxySettings s = new ProxySettings();

        SettingsBinder.bind(s, Map.of("upstreamPorts", "3128,3129"));

        assertThat(s.upstreamPorts).containsExactly(3128, 3129);
    }
}
