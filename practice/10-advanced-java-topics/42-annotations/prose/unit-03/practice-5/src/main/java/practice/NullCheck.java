package practice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class NullCheck {

    private NullCheck() {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    public @interface NonNull {
    }

    /** "<field> is null" and "<field> has a null element" problems, sorted. */
    public static List<String> problems(Object bean) {
        throw new UnsupportedOperationException("TODO");
    }
}
