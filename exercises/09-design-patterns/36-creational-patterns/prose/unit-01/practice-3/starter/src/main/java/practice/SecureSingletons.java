package practice;

import java.io.Serializable;

public final class SecureSingletons {

    private SecureSingletons() {
    }

    /** A superclass that makes its objects cloneable, as a framework base class might. */
    public static class Base implements Cloneable {
        @Override
        public Base clone() throws CloneNotSupportedException {
            return (Base) super.clone();
        }
    }

    public static final class Settings extends Base implements Serializable {

        private static final long serialVersionUID = 1L;

        private Settings() {
        }

        /** Returns the one Settings. */
        public static Settings getInstance() {
            throw new UnsupportedOperationException("write getInstance");
        }

        public String name() {
            return "settings";
        }
    }
}
