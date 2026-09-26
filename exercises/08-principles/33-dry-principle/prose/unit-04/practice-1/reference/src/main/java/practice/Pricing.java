package practice;

/** Tax and discount kept apart, and one bulk-discount rule shared by both sales channels. */
public final class Pricing {

    private Pricing() {
    }

    /** The amount with tax added: amount * (1 + taxRate). */
    public static double calculateTax(double amount, double taxRate) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must not be negative");
        }
        if (taxRate < 0 || taxRate > 1) {
            throw new IllegalArgumentException("Tax rate must be between 0 and 1");
        }
        return amount * (1 + taxRate);
    }

    /** The amount with the discount taken off: amount * (1 - discountRate). */
    public static double calculateDiscount(double amount, double discountRate) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must not be negative");
        }
        if (discountRate < 0 || discountRate > 1) {
            throw new IllegalArgumentException("Discount rate must be between 0 and 1");
        }
        return amount * (1 - discountRate);
    }

    /** The one bulk-discount rule: 5% off from 10 items, 10% from 50, 20% from 100. */
    public static double applyBulkDiscount(double price, int quantity) {
        if (quantity >= 100) {
            return price * 0.80;
        }
        if (quantity >= 50) {
            return price * 0.90;
        }
        if (quantity >= 10) {
            return price * 0.95;
        }
        return price;
    }

    /** The unit price of an online order. */
    public static double onlinePrice(double price, int quantity) {
        return applyBulkDiscount(price, quantity);
    }

    /** The unit price of an in-store order. */
    public static double inStorePrice(double price, int quantity) {
        return applyBulkDiscount(price, quantity);
    }
}
