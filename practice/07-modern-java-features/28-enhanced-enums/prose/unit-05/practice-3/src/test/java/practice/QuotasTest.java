package practice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Properties;
import org.junit.jupiter.api.Test;
import practice.Quotas.Tier;

class QuotasTest {

    private static Properties props(String... keysAndValues) {
        Properties p = new Properties();
        for (int i = 0; i < keysAndValues.length; i += 2) p.setProperty(keysAndValues[i], keysAndValues[i + 1]);
        return p;
    }

    @Test
    void everyTierGetsItsQuota() {
        Quotas q = Quotas.load(props(
                "quota.FREE", "25", "quota.TEAM", "750", "quota.BUSINESS", "4000",
                "quota.ENTERPRISE", "64000", "mail.from", "noreply@example.com"));
        assertThat(q.megabytes(Tier.FREE)).isEqualTo(25);
        assertThat(q.megabytes(Tier.TEAM)).isEqualTo(750);
        assertThat(q.megabytes(Tier.BUSINESS)).isEqualTo(4000);
        assertThat(q.megabytes(Tier.ENTERPRISE)).isEqualTo(64000);
    }

    @Test
    void aTierWithoutAQuotaStopsTheStartup() {
        Properties p = props("quota.FREE", "10", "quota.TEAM", "300", "quota.BUSINESS", "2048");
        assertThatThrownBy(() -> Quotas.load(p))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ENTERPRISE");
    }

    @Test
    void aFileWithNoQuotasNamesEveryTier() {
        Properties p = props("mail.from", "noreply@example.com");
        assertThatThrownBy(() -> Quotas.load(p))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("FREE")
                .hasMessageContaining("TEAM")
                .hasMessageContaining("BUSINESS")
                .hasMessageContaining("ENTERPRISE");
    }

    @Test
    void aMistypedTierStopsTheStartupNamingTheKey() {
        Properties p = props("quota.FREE", "10", "quota.TEAM", "300", "quota.BUSINESS", "2048",
                "quota.ENTERPRISE", "50000", "quota.BUISNESS", "2048");
        assertThatThrownBy(() -> Quotas.load(p))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("quota.BUISNESS");
    }
}
