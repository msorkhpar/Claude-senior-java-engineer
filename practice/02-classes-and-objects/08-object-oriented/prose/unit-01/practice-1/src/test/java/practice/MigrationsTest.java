package practice;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import practice.Migrations.AddEmailColumn;
import practice.Migrations.IndexEmail;
import practice.Migrations.Migration;
import practice.Migrations.SchemaHistory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MigrationsTest {

    /** A team's migration written in the test: it creates a table. */
    static class CreateRefundTable extends Migration {
        CreateRefundTable() { super("V12"); }

        @Override public void migrate(SchemaHistory history) {
            history.execute("CREATE TABLE refund (id BIGINT PRIMARY KEY)");
        }
    }

    /** A team's migration whose second statement fails. */
    static class SplitAddress extends Migration {
        SplitAddress() { super("V13"); }

        @Override public void migrate(SchemaHistory history) {
            history.execute("ALTER TABLE customer ADD street VARCHAR(200)");
            throw new IllegalArgumentException("column city already exists");
        }
    }

    @Test
    void pendingMigrationsRunInOrderAndAreRecorded() {
        SchemaHistory history = new SchemaHistory();

        for (Migration m : List.of(new AddEmailColumn(), new IndexEmail(), new CreateRefundTable())) {
            m.apply(history);
        }

        assertThat(history.statements()).containsExactly(
                "ALTER TABLE customer ADD email VARCHAR(320)",
                "CREATE INDEX customer_email ON customer(email)",
                "CREATE TABLE refund (id BIGINT PRIMARY KEY)");
        assertThat(history.appliedVersions()).containsExactly("V7", "V8", "V12");
        assertThat(history.isLocked()).isFalse();
    }

    @Test
    void aSecondDeployRunsNothingThatWasAlreadyApplied() {
        SchemaHistory history = new SchemaHistory();
        new AddEmailColumn().apply(history);
        new CreateRefundTable().apply(history);

        new AddEmailColumn().apply(history);
        new CreateRefundTable().apply(history);
        new IndexEmail().apply(history);

        assertThat(history.statements()).hasSize(3);
        assertThat(history.appliedVersions()).containsExactly("V7", "V12", "V8");
    }

    @Test
    void aMigrationDoesNotRunWhileAnotherDeployHoldsTheLock() {
        SchemaHistory history = new SchemaHistory();
        history.lock();

        assertThatThrownBy(() -> new CreateRefundTable().apply(history))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("locked by another migration");
        assertThat(history.statements()).isEmpty();
        assertThat(history.isApplied("V12")).isFalse();
    }

    @Test
    void noTeamCanReplaceTheWayAMigrationIsApplied() throws Exception {
        Method apply = Migration.class.getMethod("apply", SchemaHistory.class);

        assertThat(Modifier.isFinal(apply.getModifiers()))
                .as("a subclass can override apply(...) and skip the history or the lock")
                .isTrue();
    }

    @Test
    void aMigrationsStatementsCanOnlyBeRunThroughApply() throws Exception {
        Method base = Migration.class.getDeclaredMethod("migrate", SchemaHistory.class);
        assertThat(Modifier.isProtected(base.getModifiers()))
                .as("Migration.migrate must be callable by subclasses in any package, and by no other code there")
                .isTrue();
        for (Class<?> shipped : List.of(AddEmailColumn.class, IndexEmail.class)) {
            Method m = shipped.getDeclaredMethod("migrate", SchemaHistory.class);
            assertThat(Modifier.isPublic(m.getModifiers()))
                    .as(shipped.getSimpleName() + ".migrate can be called by any code holding one")
                    .isFalse();
        }
    }

    @Test
    void aMigrationThatFailsIsNotRecordedAsApplied() {
        SchemaHistory history = new SchemaHistory();
        new AddEmailColumn().apply(history);

        assertThatThrownBy(() -> new SplitAddress().apply(history))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("column city already exists");

        assertThat(history.isApplied("V13")).isFalse();
        assertThat(history.appliedVersions()).containsExactly("V7");
    }

    @Test
    void aMigrationThatFailsReleasesTheLock() {
        SchemaHistory history = new SchemaHistory();

        assertThatThrownBy(() -> new SplitAddress().apply(history))
                .isInstanceOf(IllegalArgumentException.class);

        assertThat(history.isLocked()).isFalse();
        new CreateRefundTable().apply(history);
        assertThat(history.appliedVersions()).containsExactly("V12");
    }
}
