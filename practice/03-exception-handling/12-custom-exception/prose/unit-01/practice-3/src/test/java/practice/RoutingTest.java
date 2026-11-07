package practice;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class RoutingTest {

    private static Routing.Rule refuses(String name) {
        return (parcelId, postcode) -> {
            throw new Routing.NoRouteException(name, parcelId);
        };
    }

    private static Routing.Rule servesPrefix(String prefix, String hub) {
        return (parcelId, postcode) -> {
            if (postcode.startsWith(prefix)) return hub;
            throw new Routing.NoRouteException("prefix-" + prefix, parcelId);
        };
    }

    @Test
    void theFirstRuleThatAppliesRoutesTheParcel() {
        Routing routing = new Routing(List.of(
                refuses("oversize"), servesPrefix("69", "hub-LYS"), servesPrefix("1", "hub-MRS")));

        assertThat(routing.route("P-88213", "69003")).isEqualTo("hub-LYS");
        assertThat(routing.route("P-88214", "13001")).isEqualTo("hub-MRS");
    }

    @Test
    void aParcelNoRuleAppliesToIsReportedToTheCaller() {
        Routing routing = new Routing(List.of(refuses("oversize"), servesPrefix("69", "hub-LYS")));

        Throwable failure = catchThrowable(() -> routing.route("P-40517", "29200"));

        assertThat(failure).isInstanceOf(RuntimeException.class);
        assertThat(failure.getMessage()).contains("P-40517");
    }

    @Test
    void aRefusalCostsNoStackWalk() {
        Routing.NoRouteException miss = new Routing.NoRouteException("hazmat", "P-11902");

        assertThat(miss.getStackTrace()).isEmpty();
        assertThat(miss.getMessage()).contains("hazmat").contains("P-11902");
    }

    @Test
    void theReportedFailureShowsWhereItWasThrown() {
        Routing routing = new Routing(List.of(refuses("oversize"), refuses("hazmat")));

        Throwable failure = catchThrowable(() -> routing.route("P-40518", "29200"));

        assertThat(failure).isNotNull();
        assertThat(failure.getMessage()).contains("P-40518");
        assertThat(failure.getStackTrace()).isNotEmpty();
    }

    @Test
    void theReportedFailureSaysWhichRuleRefusedLast() {
        Routing routing = new Routing(List.of(refuses("oversize"), refuses("hazmat")));

        Throwable failure = catchThrowable(() -> routing.route("P-40519", "29200"));

        assertThat(failure).isNotNull();
        assertThat(failure.getCause()).isInstanceOf(Routing.NoRouteException.class);
        assertThat(failure.getCause().getMessage()).contains("hazmat");
    }
}
