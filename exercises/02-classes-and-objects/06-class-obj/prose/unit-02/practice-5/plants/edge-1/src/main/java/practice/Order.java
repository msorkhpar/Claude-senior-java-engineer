package practice;

import java.util.Objects;

public class Order {
    private final String customer;
    private final int quantity;
    private final String note;
    private final boolean express;

    private Order(Builder builder) {
        this.customer = builder.customer;
        this.quantity = builder.quantity;
        this.note = builder.note;
        this.express = builder.express;
    }

    public static Builder builder(String customer) {
        return new Builder(customer);
    }

    public String getCustomer() {
        return customer;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getNote() {
        return note;
    }

    public boolean isExpress() {
        return express;
    }

    public static class Builder {
        private final String customer;
        private int quantity = 1;
        private String note;
        private boolean express = false;

        private Builder(String customer) {
            this.customer = Objects.requireNonNull(customer, "customer");
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder note(String note) {
            this.note = note;
            return this;
        }

        public Builder express(boolean express) {
            this.express = express;
            return this;
        }

        public Order build() {
            if (quantity < 1) {
                throw new IllegalArgumentException("Quantity must be at least 1");
            }
            return new Order(this);
        }
    }
}
