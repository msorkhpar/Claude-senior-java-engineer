package practice;

import java.lang.annotation.Annotation;
import java.lang.annotation.RetentionPolicy;

public final class Retentions {

    private Retentions() {
    }

    /** The retention policy of the annotation type. */
    public static RetentionPolicy policyOf(Class<? extends Annotation> type) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Whether an annotation of type still exists at the given stage. */
    public static boolean availableIn(Class<? extends Annotation> type, RetentionPolicy stage) {
        throw new UnsupportedOperationException("TODO");
    }
}
