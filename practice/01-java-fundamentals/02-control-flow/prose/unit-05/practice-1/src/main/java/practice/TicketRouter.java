package practice;

import java.util.List;

/** Puts each support ticket in the queue of the team that handles it, and pages people for urgent ones. */
public class TicketRouter {

    public enum Category { BILLING, REFUND, OUTAGE, SECURITY, FEATURE_REQUEST, OTHER }

    /** Severity 1 is the most urgent, 4 the least. */
    public record Ticket(String id, Category category, int severity) {}

    private final List<String> pages;

    /** Every page the router sends is added to {@code pages}, as "who: ticket id". */
    public TicketRouter(List<String> pages) {
        this.pages = pages;
    }

    /** Returns the queue the ticket goes to, paging whoever must know about it now. */
    public String route(Ticket ticket) {
        // TODO: Billing and refund tickets go to "finance"; feature requests and anything else
        //       go to "product". An outage goes to "operations", and when its severity is 1 or 2
        //       the on-call engineer is paged too. A security ticket goes to "security" and
        //       always pages the security lead. Nobody else is paged.
        throw new IllegalStateException("TODO: route");
    }
}
