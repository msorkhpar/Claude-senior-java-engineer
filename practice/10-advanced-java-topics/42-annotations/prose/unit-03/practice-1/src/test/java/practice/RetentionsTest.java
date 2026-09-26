package practice;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.Test;

import static java.lang.annotation.RetentionPolicy.CLASS;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.RetentionPolicy.SOURCE;
import static org.assertj.core.api.Assertions.assertThat;

class RetentionsTest {

    @Retention(RetentionPolicy.CLASS)
    @interface Generated {
    }

    @interface Plain {
    }

    @Test
    void readsTheDeclaredRetention() {
        assertThat(Retentions.policyOf(Deprecated.class)).isEqualTo(RUNTIME);
        assertThat(Retentions.policyOf(Override.class)).isEqualTo(SOURCE);
        assertThat(Retentions.policyOf(Generated.class)).isEqualTo(CLASS);
        assertThat(Retentions.availableIn(Deprecated.class, RUNTIME)).isTrue();
        assertThat(Retentions.availableIn(Override.class, SOURCE)).isTrue();
        assertThat(Retentions.availableIn(Override.class, RUNTIME)).isFalse();
    }

    @Test
    void noRetentionMeansClass() {
        assertThat(Retentions.policyOf(Plain.class)).isEqualTo(CLASS);
        assertThat(Retentions.availableIn(Plain.class, RUNTIME)).isFalse();
    }

    @Test
    void aLongerRetentionIncludesTheShorterStages() {
        assertThat(Retentions.availableIn(Deprecated.class, SOURCE)).isTrue();
        assertThat(Retentions.availableIn(Deprecated.class, CLASS)).isTrue();
        assertThat(Retentions.availableIn(Generated.class, SOURCE)).isTrue();
        assertThat(Retentions.availableIn(Generated.class, RUNTIME)).isFalse();
        assertThat(Retentions.availableIn(Override.class, CLASS)).isFalse();
    }
}
