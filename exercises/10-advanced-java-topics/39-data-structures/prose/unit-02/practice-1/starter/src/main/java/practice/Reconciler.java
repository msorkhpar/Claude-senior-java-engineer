package practice;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Month-end reconciliation: which shipped items were never billed? Items are compared with
 * equals() and hashCode(); a shipment can list the same item more than once.
 */
public class Reconciler {

    /** The line the month-end report prints. */
    public static <T> String summary(List<T> shipped, Collection<T> billed) {
        List<T> missing = unbilled(shipped, billed);
        return missing.size() + " of " + shipped.size() + " shipped lines not billed";
    }

    /**
     * The shipped items that do not appear among the billed ones, in shipping order, repeats
     * included. Neither argument may be changed.
     */
    static <T> List<T> unbilled(List<T> shipped, Collection<T> billed) {
        // TODO: A month has hundreds of thousands of shipped lines and as many billed ones, and
        //       this step must not be what makes reconciliation slow down as the business grows.
        throw new IllegalStateException("TODO: unbilled");
    }
}
