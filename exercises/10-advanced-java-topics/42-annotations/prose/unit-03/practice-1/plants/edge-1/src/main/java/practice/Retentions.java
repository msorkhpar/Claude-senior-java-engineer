package practice;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class Retentions {

    private Retentions() {
    }

    /** The retention policy of the annotation type. */
    public static RetentionPolicy policyOf(Class<? extends Annotation> type) {
        Retention retention = type.getAnnotation(Retention.class);
        return retention == null ? RetentionPolicy.RUNTIME : retention.value();
    }

    /** Whether an annotation of type still exists at the given stage. */
    public static boolean availableIn(Class<? extends Annotation> type, RetentionPolicy stage) {
        return policyOf(type).compareTo(stage) >= 0;
    }
}
