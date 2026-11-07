package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EventIngestTest {

    private final List<String> archived = new ArrayList<>();
    private final List<String> announced = new ArrayList<>();
    private final EventIngest ingest = new EventIngest(archived::add, announced::add);

    @Test
    void announcesEachNewEventAndCountsTheBatch() {
        long received = ingest.ingest(List.of("dev3:boot", "dev3:temp-71", "dev8:boot"));

        assertThat(received).isEqualTo(3);
        assertThat(announced).containsExactlyInAnyOrder("dev3:boot", "dev3:temp-71", "dev8:boot");
    }

    @Test
    void theArchiveGetsEveryIdTheDevicesSent() {
        ingest.ingest(List.of("dev5:door-open", "dev5:door-open", "dev2:low-battery"));
        ingest.ingest(List.of("dev2:low-battery"));

        assertThat(archived).containsExactly("dev5:door-open", "dev5:door-open", "dev2:low-battery",
                "dev2:low-battery");
    }

    @Test
    void anEventIsAnnouncedOnlyTheFirstTimeItArrives() {
        long first = ingest.ingest(List.of("dev5:door-open", "dev5:door-open", "dev2:low-battery"));
        long second = ingest.ingest(List.of("dev2:low-battery", "dev9:boot"));

        assertThat(first).isEqualTo(3);
        assertThat(second).isEqualTo(2);
        assertThat(announced).containsExactlyInAnyOrder("dev5:door-open", "dev2:low-battery", "dev9:boot");
    }
}
