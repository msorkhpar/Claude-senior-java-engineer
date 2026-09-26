package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class BreadcrumbsTest {

    @Test
    void joinsTheParts() {
        assertThat(Breadcrumbs.trail("Home", "Docs", "Java")).isEqualTo("Home / Docs / Java");
        assertThat(Breadcrumbs.trail("Home")).isEqualTo("Home");
        assertThat(Breadcrumbs.trail()).isEmpty();
    }

    @Test
    void nullPartsAreLeftOut() {
        assertThat(Breadcrumbs.trail("Home", null, "Java")).isEqualTo("Home / Java");
        assertThat(Breadcrumbs.trail((String) null)).isEmpty();
    }

    @Test
    void aLeadingNullLeavesNoSeparator() {
        assertThat(Breadcrumbs.trail(null, "Docs")).isEqualTo("Docs");
    }
}
