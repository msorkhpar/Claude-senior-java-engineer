package practice;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import practice.LimiterWiring.RateLimiter;
import practice.LimiterWiring.Settings;
import practice.LimiterWiring.SettingsHolder;

class LimiterWiringTest {

    private static int admitted(RateLimiter limiter, int attempts) {
        int admitted = 0;
        for (int i = 0; i < attempts; i++) {
            if (limiter.tryAdmit()) admitted++;
        }
        return admitted;
    }

    @Test
    void theLimiterAdmitsUpToTheLoadedLimit() {
        SettingsHolder holder = new SettingsHolder();
        holder.reload(new Settings(3, "ap-south"));
        RateLimiter limiter = LimiterWiring.limiter(holder);
        assertThat(admitted(limiter, 10)).isEqualTo(3);
        limiter.nextWindow();
        assertThat(admitted(limiter, 10)).isEqualTo(3);
    }

    @Test
    void everyHeaderOfARunNamesTheRegion() {
        SettingsHolder holder = new SettingsHolder();
        holder.reload(new Settings(40, "sa-east"));
        Supplier<String> region = LimiterWiring.exportHeaderRegion(holder);
        assertThat(region.get()).isEqualTo("sa-east");
        assertThat(region.get()).isEqualTo("sa-east");
    }

    @Test
    void theLimiterFollowsAReload() {
        SettingsHolder holder = new SettingsHolder();
        holder.reload(new Settings(2, "ap-south"));
        RateLimiter limiter = LimiterWiring.limiter(holder);
        assertThat(admitted(limiter, 10)).isEqualTo(2);
        holder.reload(new Settings(5, "ap-south"));
        limiter.nextWindow();
        assertThat(admitted(limiter, 10)).isEqualTo(5);
    }

    @Test
    void theLimiterCanBeBuiltBeforeTheFirstLoad() {
        SettingsHolder holder = new SettingsHolder();
        RateLimiter limiter = LimiterWiring.limiter(holder);
        holder.reload(new Settings(4, "me-central"));
        assertThat(admitted(limiter, 10)).isEqualTo(4);
    }

    @Test
    void aReloadDuringARunDoesNotChangeItsHeaders() {
        SettingsHolder holder = new SettingsHolder();
        holder.reload(new Settings(40, "eu-north"));
        Supplier<String> region = LimiterWiring.exportHeaderRegion(holder);
        assertThat(region.get()).isEqualTo("eu-north");
        holder.reload(new Settings(40, "us-west"));
        assertThat(region.get()).isEqualTo("eu-north");
    }
}
