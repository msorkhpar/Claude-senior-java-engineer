package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeployTextsTest {

    @Test
    void theCrontabListsTheJobsInOrder() {
        assertThat(DeployTexts.crontab().lines().toList()).containsExactly(
                "# m h dom mon dow command",
                "15 2 * * * /opt/backup/run.sh",
                "0 * * * * /opt/metrics/flush.sh",
                "0 4 * * 0 /opt/logs/rotate.sh");
    }

    @Test
    void theStringToSignHoldsMethodPathAndDate() {
        assertThat(DeployTexts.stringToSign("PUT", "/artifacts/billing-3.2.jar", "2026-11-06T10:15:00Z")
                .lines().toList())
                .containsExactly("PUT", "/artifacts/billing-3.2.jar", "2026-11-06T10:15:00Z");
        assertThat(DeployTexts.stringToSign("DELETE", "/artifacts/old.jar", "2026-01-31T23:59:59Z")
                .lines().toList())
                .containsExactly("DELETE", "/artifacts/old.jar", "2026-01-31T23:59:59Z");
    }

    @Test
    void theLastJobEndsWithALineBreakToo() {
        assertThat(DeployTexts.crontab()).isEqualTo(
                "# m h dom mon dow command\n"
                        + "15 2 * * * /opt/backup/run.sh\n"
                        + "0 * * * * /opt/metrics/flush.sh\n"
                        + "0 4 * * 0 /opt/logs/rotate.sh\n");
    }

    @Test
    void nothingFollowsTheDateInTheSignedString() {
        assertThat(DeployTexts.stringToSign("GET", "/artifacts/search-1.0.jar", "2026-07-04T08:00:00Z"))
                .isEqualTo("GET\n/artifacts/search-1.0.jar\n2026-07-04T08:00:00Z");
    }
}
