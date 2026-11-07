package practice;

/** The size and weight checks the depot's intake scanner runs on every parcel. */
public class Depot {

    /** A parcel that goes on the belt: up to 2,000 g, and up to 40 cm on its longest side. */
    public static class Parcel {
        protected int maxGrams = 2_000;
        protected int maxCentimetres = 40;

        /** Whether the depot takes a parcel of this weight and longest side. */
        public boolean accepts(int grams, int centimetres) {
            return grams > 0 && grams <= maxGrams
                    && centimetres > 0 && centimetres <= maxCentimetres;
        }

        /** The limits the intake screen shows for this kind of parcel. */
        public String limits() {
            return "up to " + maxGrams + " g, " + maxCentimetres + " cm";
        }
    }

    /** A parcel that goes on a pallet. */
    public static class FreightParcel extends Parcel {
        public FreightParcel() {
            maxGrams = 30_000;
            maxCentimetres = 150;
        }
    }
}
