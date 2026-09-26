package practice;

public class Order {

    public static Builder builder(String customer) {
        throw new UnsupportedOperationException("write builder");
    }

    public String getCustomer() {
        throw new UnsupportedOperationException("write getCustomer");
    }

    public int getQuantity() {
        throw new UnsupportedOperationException("write getQuantity");
    }

    public String getNote() {
        throw new UnsupportedOperationException("write getNote");
    }

    public boolean isExpress() {
        throw new UnsupportedOperationException("write isExpress");
    }

    public static class Builder {

        public Builder quantity(int quantity) {
            throw new UnsupportedOperationException("write quantity");
        }

        public Builder note(String note) {
            throw new UnsupportedOperationException("write note");
        }

        public Builder express(boolean express) {
            throw new UnsupportedOperationException("write express");
        }

        public Order build() {
            throw new UnsupportedOperationException("write build");
        }
    }
}
