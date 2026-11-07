package practice;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SignupRulesTest {

    private static SignupRules rules() {
        return new SignupRules(new HashSet<>(List.of("postmaster", "webmaster", "abuse", "noc")));
    }

    /** Eight limits, built so that their iteration order is not their order by name. */
    private static Map<String, Integer> limits() {
        return Map.of(
                "trial-days", 30, "seats-max", 25, "api-keys", 4, "webhooks", 12,
                "exports-per-day", 6, "invites-per-hour", 40, "ip-allowlist", 16, "domains", 3);
    }

    @Test
    void reservedNamesAreRefusedAndOthersAccepted() {
        SignupRules rules = rules();

        assertThat(rules.isReserved("postmaster")).isTrue();
        assertThat(rules.isReserved("noc")).isTrue();
        assertThat(rules.isReserved("j.doe")).isFalse();
        assertThat(rules.isReserved("Postmaster ")).isFalse();
    }

    @Test
    void aRequestWithoutAUsernameIsNotReserved() {
        SignupRules rules = rules();

        assertThat(rules.isReserved(null)).isFalse();
    }

    @Test
    void theExportGetsTheBlockedDomainsInOrder() {
        SignupRules rules = rules();
        rules.block("mailinator.example");
        rules.block("tempinbox.example");
        rules.block("throwaway.example");

        assertThat(rules.blockedDomains())
                .containsExactly("mailinator.example", "tempinbox.example", "throwaway.example");
    }

    @Test
    void blocksMadeWhileTheExportRunsDoNotChangeWhatItWrites() {
        SignupRules rules = rules();
        rules.block("mailinator.example");
        rules.block("tempinbox.example");

        List<String> exported = rules.blockedDomains();
        rules.block("throwaway.example");

        assertThat(exported).containsExactly("mailinator.example", "tempinbox.example");
        assertThat(rules.isBlocked("throwaway.example")).isTrue();
    }

    @Test
    void theReportHasOneLinePerLimit() {
        String report = SignupRules.limitsReport(limits());

        assertThat(report.split("\n")).containsExactlyInAnyOrder(
                "trial-days=30", "seats-max=25", "api-keys=4", "webhooks=12",
                "exports-per-day=6", "invites-per-hour=40", "ip-allowlist=16", "domains=3");
    }

    @Test
    void theReportListsTheLimitsByName() {
        Map<String, Integer> other = new LinkedHashMap<>();
        other.put("sso", 1);
        other.put("audit-days", 90);
        other.put("projects", 50);

        assertThat(SignupRules.limitsReport(limits())).isEqualTo(String.join("\n",
                "api-keys=4", "domains=3", "exports-per-day=6", "invites-per-hour=40",
                "ip-allowlist=16", "seats-max=25", "trial-days=30", "webhooks=12"));
        assertThat(SignupRules.limitsReport(Map.copyOf(other)))
                .isEqualTo("audit-days=90\nprojects=50\nsso=1");
    }
}
