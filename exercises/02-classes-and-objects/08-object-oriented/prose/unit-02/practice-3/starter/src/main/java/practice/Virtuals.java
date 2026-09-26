package practice;

public class Virtuals {

    public static class Base {
        public String kind() {
            throw new UnsupportedOperationException("write Base.kind");
        }

        public static String label() {
            throw new UnsupportedOperationException("write Base.label");
        }

        public String introduce() {
            throw new UnsupportedOperationException("write introduce");
        }

        private String secret() {
            throw new UnsupportedOperationException("write Base.secret");
        }
    }

    public static class Derived extends Base {
        @Override
        public String kind() {
            throw new UnsupportedOperationException("write Derived.kind");
        }

        public static String label() {
            throw new UnsupportedOperationException("write Derived.label");
        }

        private String secret() {
            throw new UnsupportedOperationException("write Derived.secret");
        }
    }

    /** The kind of the object b refers to. */
    public static String kindOf(Base b) {
        throw new UnsupportedOperationException("write kindOf");
    }
}
