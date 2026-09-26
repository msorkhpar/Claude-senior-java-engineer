package practice;

/** Shipping prices, with the per-kilogram rate and the delivery fee named once. */
public final class ShippingRates {

    private final double costPerKg;
    private final double baseDeliveryFee;

    /** Rates with the given cost per kilogram and base delivery fee. */
    public ShippingRates(double costPerKg, double baseDeliveryFee) {
        this.costPerKg = costPerKg;
        this.baseDeliveryFee = baseDeliveryFee;
    }

    /** The standard rates: 0.15 per kilogram and a base delivery fee of 5.0. */
    public static ShippingRates standard() {
        return new ShippingRates(0.15, 5.0);
    }

    /** The shipping cost of a parcel of this weight. */
    public double shippingCost(double weightKg) {
        if (weightKg < 0) {
            throw new IllegalArgumentException("weight must not be negative");
        }
        return weightKg * costPerKg;
    }

    /** The delivery estimate: the shipping cost plus the base delivery fee. */
    public double deliveryEstimate(double weightKg) {
        return weightKg * 0.15 + baseDeliveryFee;
    }
}
