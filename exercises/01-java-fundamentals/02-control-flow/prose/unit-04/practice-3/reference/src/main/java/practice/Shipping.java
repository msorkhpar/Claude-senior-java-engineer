package practice;

public final class Shipping {

    private Shipping() {
    }

    /** Returns the delivery fee for zone 1, 2 or 3. */
    public static int fee(int zone) {
        int fee;
        switch (zone) {
            case 1:
                fee = 5;
                break;
            case 2:
                fee = 8;
                break;
            case 3:
                fee = 12;
                break;
            default:
                throw new IllegalArgumentException("unknown zone " + zone);
        }
        return fee;
    }
}
