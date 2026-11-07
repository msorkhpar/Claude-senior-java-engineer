package practice;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An issued invoice. The monthly billing run sets up one Builder per plan with the plan's lines,
 * then builds each customer's invoice from it.
 */
public final class Invoice {

    private final String customer;
    private final List<String> lines;

    private Invoice(Builder b) {
        this.customer = b.customer;
        this.lines = keep(b.lines);
    }

    /** The lines this invoice keeps, given the builder's lines at build(). */
    static List<String> keep(List<String> builderLines) {
        // TODO: An invoice, once issued, must read the same whatever happens to the builder it
        //       came from or to the list its lines() returns.
        throw new IllegalStateException("TODO: keep");
    }

    public static Builder builder() {
        return new Builder();
    }

    public String customer() { return customer; }
    public List<String> lines() { return lines; }

    public static final class Builder {
        private String customer;
        private List<String> lines = new ArrayList<>();

        private Builder() {}

        public Builder customer(String customer) { this.customer = customer; return this; }

        public Builder line(String line) {
            lines.add(Objects.requireNonNull(line, "line"));
            return this;
        }

        /** A builder with this one's customer and lines, to be filled in further on its own. */
        public Builder copy() {
            // TODO: The billing run fills in each customer's copy of a plan's builder. Nothing done
            //       to a copy may show up in the plan's builder or in another copy.
            throw new IllegalStateException("TODO: copy");
        }

        public Invoice build() {
            return new Invoice(this);
        }
    }
}
