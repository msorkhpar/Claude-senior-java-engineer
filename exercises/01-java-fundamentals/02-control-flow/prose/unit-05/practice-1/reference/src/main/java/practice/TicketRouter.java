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
        return switch (ticket.category()) {
            case BILLING, REFUND -> "finance";
            case OUTAGE -> {
                if (ticket.severity() <= 2) pages.add("on-call: " + ticket.id());
                yield "operations";
            }
            case SECURITY -> {
                pages.add("security-lead: " + ticket.id());
                yield "security";
            }
            case FEATURE_REQUEST, OTHER -> "product";
        };
    }
}
