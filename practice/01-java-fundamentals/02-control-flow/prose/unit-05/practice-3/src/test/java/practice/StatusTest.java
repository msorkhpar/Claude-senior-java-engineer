package practice;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class StatusTest {

    @Test
    void mapsEachStatus() {
        List<String> alerts = new ArrayList<>();
        assertThat(Status.code("success", alerts)).isEqualTo(1);
        assertThat(Status.code("error", alerts)).isEqualTo(-1);
        assertThat(Status.code("pending", alerts)).isZero();
    }

    @Test
    void anErrorRaisesOneAlert() {
        List<String> alerts = new ArrayList<>();
        Status.code("error", alerts);
        assertThat(alerts).containsExactly("alert: error");
        Status.code("success", alerts);
        Status.code("pending", alerts);
        assertThat(alerts).containsExactly("alert: error");
    }

    @Test
    void caseDoesNotMatter() {
        List<String> alerts = new ArrayList<>();
        assertThat(Status.code("SUCCESS", alerts)).isEqualTo(1);
        assertThat(Status.code("Error", alerts)).isEqualTo(-1);
        assertThat(alerts).containsExactly("alert: Error");
    }
}
