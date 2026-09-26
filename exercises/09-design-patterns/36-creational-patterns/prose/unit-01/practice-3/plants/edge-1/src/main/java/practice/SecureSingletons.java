package practice;

import java.io.Serial;
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

        @Serial
        private static final long serialVersionUID = 1L;

        private static boolean instantiated = false;
        private static final Settings INSTANCE = new Settings();

        private Settings() {
            if (instantiated) {
                throw new IllegalStateException("Use getInstance()");
            }
            instantiated = true;
        }

        /** Returns the one Settings. */
        public static Settings getInstance() {
            return INSTANCE;
        }

        public String name() {
            return "settings";
        }

        @Override
        public Base clone() throws CloneNotSupportedException {
            throw new CloneNotSupportedException("Settings is a singleton");
        }
    }
}
