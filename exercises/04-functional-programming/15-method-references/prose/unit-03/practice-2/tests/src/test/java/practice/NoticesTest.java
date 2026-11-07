package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoticesTest {

    private final Notices notices = new Notices();

    @Test
    void fillsTheTemplateWithASingleValue() {
        assertThat(notices.notice("volume %s is full", "vol-3b")).isEqualTo("volume vol-3b is full");
        assertThat(notices.notice("%d jobs waiting", 118)).isEqualTo("118 jobs waiting");
    }

    @Test
    void fillsOneSpecifierPerValueOfAnObjectArray() {
        Object[] payload = {"cache-2", 4, "oom"};

        assertThat(notices.notice("%s restarted %d times (%s)", payload))
                .isEqualTo("cache-2 restarted 4 times (oom)");
    }

    @Test
    void fillsOneSpecifierPerValueOfAnIntArray() {
        int[] payload = {2, 5};

        assertThat(notices.notice("%d of %d replicas healthy", payload)).isEqualTo("2 of 5 replicas healthy");
    }
}
