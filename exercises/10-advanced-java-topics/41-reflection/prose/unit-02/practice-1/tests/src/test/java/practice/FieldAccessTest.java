package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FieldAccessTest {

    static class Point {
        public static final String KIND = "point";
        public int x;
        public int y;
        public String label;
    }

    static class Account {
        private String owner;
        private int balance;

        Account(String owner, int balance) {
            this.owner = owner;
            this.balance = balance;
        }
    }

    static class Savings extends Account {
        private double rate;

        Savings(String owner, int balance, double rate) {
            super(owner, balance);
            this.rate = rate;
        }
    }

    @Test
    void readsAndWritesPublicFields() throws Exception {
        Point point = new Point();
        point.x = 3;

        assertThat(FieldAccess.read(point, "x")).isEqualTo(3);
        assertThat(FieldAccess.read(point, "label")).isNull();
        FieldAccess.write(point, "y", 4000);
        assertThat(point.y).isEqualTo(4000);
        assertThat(FieldAccess.readStatic(Point.class, "KIND")).isEqualTo("point");
    }

    @Test
    void readsAPrivateField() throws Exception {
        Account account = new Account(new String("ann"), 1000);

        assertThat(FieldAccess.read(account, "owner")).isEqualTo("ann");
        assertThat(FieldAccess.read(account, "balance")).isEqualTo(1000);
    }

    @Test
    void writesAPrivateField() throws Exception {
        Account account = new Account("ann", 1000);

        FieldAccess.write(account, "balance", 2500);

        assertThat(account.balance).isEqualTo(2500);
    }

    @Test
    void findsAFieldDeclaredOnASuperclass() throws Exception {
        Savings savings = new Savings(new String("bo"), 5000, 0.5);

        assertThat(FieldAccess.read(savings, "owner")).isEqualTo("bo");
        assertThat(FieldAccess.read(savings, "rate")).isEqualTo(0.5);
    }

    @Test
    void aMissingFieldIsReported() {
        assertThatThrownBy(() -> FieldAccess.read(new Point(), "z"))
                .isInstanceOf(NoSuchFieldException.class);
    }
}
