package practice;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class Kit {

    private Kit() {
    }

    public enum HttpMethod { GET, POST, PUT, DELETE }

    // TODO: every annotation below still has the default CLASS retention and no target.

    public @interface ThreadSafe {
    }

    public @interface Author {
        String value();
    }

    public @interface ApiEndpoint {
        String path();

        HttpMethod method();

        String description();

        int version();

        String[] produces();
    }

    public @interface Roles {
        Role[] value();
    }

    public @interface Role {
        String value();
    }

    public @interface Auditable {
        String level();
    }
}
