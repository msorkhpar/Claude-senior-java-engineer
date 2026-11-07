package practice;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static practice.TicketRouter.Category.*;

class TicketRouterTest {

    private final List<String> pages = new ArrayList<>();
    private final TicketRouter router = new TicketRouter(pages);

    private String route(String id, TicketRouter.Category category, int severity) {
        return router.route(new TicketRouter.Ticket(id, category, severity));
    }

    @Test
    void routesEachTeamsTicketsToItsQueue() {
        assertThat(route("T-301", BILLING, 3)).isEqualTo("finance");
        assertThat(route("T-302", REFUND, 1)).isEqualTo("finance");
        assertThat(route("T-303", FEATURE_REQUEST, 4)).isEqualTo("product");
        assertThat(route("T-304", OTHER, 2)).isEqualTo("product");
        assertThat(pages).isEmpty();

        assertThat(route("T-305", SECURITY, 4)).isEqualTo("security");
        assertThat(pages).containsExactly("security-lead: T-305");
    }

    @Test
    void outagesGoToOperationsAndPageOnCallOnlyWhenUrgent() {
        assertThat(route("T-410", OUTAGE, 1)).isEqualTo("operations");
        assertThat(route("T-411", OUTAGE, 3)).isEqualTo("operations");
        assertThat(route("T-412", OUTAGE, 2)).isEqualTo("operations");

        assertThat(pages).containsExactly("on-call: T-410", "on-call: T-412");
    }
}
