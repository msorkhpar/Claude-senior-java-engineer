package practice;
public final class Pricing {
    private Pricing() {}
    public static double calculateTax(double amount, double taxRate) { if (amount < 0) throw new IllegalArgumentException("Amount must not be negative"); if (taxRate < 0 || taxRate > 1) throw new IllegalArgumentException("Tax rate must be between 0 and 1"); return amount * (1 + taxRate); }
    public static double calculateDiscount(double amount, double discountRate) { if (amount < 0) throw new IllegalArgumentException("Amount must not be negative"); if (discountRate < 0 || discountRate > 1) throw new IllegalArgumentException("Discount rate must be between 0 and 1"); return amount * (1 - discountRate); }
    public static double applyBulkDiscount(double price, int quantity) { if (quantity >= 100) return price * 0.80; if (quantity >= 50) return price * 0.90; if (quantity >= 10) return price * 0.95; return price; }
    public static double onlinePrice(double price, int quantity) { return applyBulkDiscount(price, quantity); }
    public static double inStorePrice(double price, int quantity) { if (quantity >= 500) return price * 0.75; return applyBulkDiscount(price, quantity); }
}
