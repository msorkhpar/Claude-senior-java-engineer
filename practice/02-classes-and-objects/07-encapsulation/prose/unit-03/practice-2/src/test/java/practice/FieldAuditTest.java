package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unused")
class FieldAuditTest {

    static class Account {
        public double balance;
        private String id;
    }

    static class Shared {
        protected int level;
        int size;
        private int hidden;
    }

    static class Limits {
        public static final int MAX = 5;
        static final String NAME = "limits";
        private static final int MIN = 1;
    }

    static class Registry {
        public static int created;
        private static int seen;
    }

    static class Outer {
        private int value = 3;

        class Inner {
            private int copy;

            int read() {
                return value;
            }
        }
    }

    @Test
    void flagsAPublicField() {
        assertThat(FieldAudit.exposed(Account.class)).containsExactly("balance");
    }

    @Test
    void packageAndProtectedFieldsAreExposed() {
        assertThat(FieldAudit.exposed(Shared.class)).containsExactly("level", "size");
    }

    @Test
    void constantsAreAllowed() {
        assertThat(FieldAudit.exposed(Limits.class)).isEmpty();
    }

    @Test
    void aStaticFieldThatCanChangeIsExposed() {
        assertThat(FieldAudit.exposed(Registry.class)).containsExactly("created");
    }

    @Test
    void fieldsTheCompilerAddsAreIgnored() {
        assertThat(FieldAudit.exposed(Outer.Inner.class)).isEmpty();
    }
}
