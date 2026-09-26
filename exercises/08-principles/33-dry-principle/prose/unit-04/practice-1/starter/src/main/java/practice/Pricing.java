package practice;

/** Tax and discount kept apart, and one bulk-discount rule shared by both sales channels. */
public final class Pricing {

    private Pricing() {
    }

    /** The amount with tax added: amount * (1 + taxRate). */
    public static double calculateTax(double amount, double taxRate) {
        throw new UnsupportedOperationException("write calculateTax");
    }

    /** The amount with the discount taken off: amount * (1 - discountRate). */
    public static double calculateDiscount(double amount, double discountRate) {
        throw new UnsupportedOperationException("write calculateDiscount");
    }

    /** The one bulk-discount rule: 5% off from 10 items, 10% from 50, 20% from 100. */
    public static double applyBulkDiscount(double price, int quantity) {
        throw new UnsupportedOperationException("write applyBulkDiscount");
    }

    /** The unit price of an online order. */
    public static double onlinePrice(double price, int quantity) {
        throw new UnsupportedOperationException("write onlinePrice");
    }

    /** The unit price of an in-store order. */
    public static double inStorePrice(double price, int quantity) {
        throw new UnsupportedOperationException("write inStorePrice");
    }
}
