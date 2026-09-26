package practice;

public class Virtuals {

    public static class Base {
        public String kind() {
            return "base";
        }

        public static String label() {
            return "Base";
        }

        public String introduce() {
            return "I am " + secret();
        }

        protected String secret() {
            return "base";
        }
    }

    public static class Derived extends Base {
        @Override
        public String kind() {
            return "derived";
        }

        public static String label() {
            return "Derived";
        }

        protected String secret() {
            return "derived";
        }
    }

    /** The kind of the object b refers to. */
    public static String kindOf(Base b) {
        return b.kind();
    }
}
