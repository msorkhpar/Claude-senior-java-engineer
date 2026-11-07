package practice;

import org.junit.jupiter.api.Test;
import practice.PermissionGuard.Permission;

import java.lang.reflect.Method;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionGuardTest {

    static class Invoices {
        @Permission("invoices:read")
        public void listInvoices() { }

        @Permission("invoices:write")
        @Permission("billing:admin")
        public void issueCredit() { }

        public void health() { }
    }

    @Permission("exports:run")
    @Permission("billing:admin")
    static class Exports {
        public void runExport() { }
    }

    @Permission("reports:read")
    static class Reports {
        public void monthly() { }
    }

    private final PermissionGuard guard = new PermissionGuard();

    private static Method method(Class<?> type, String name) throws NoSuchMethodException {
        return type.getDeclaredMethod(name);
    }

    @Test
    void aCallerHoldingTheOnlyPermissionGetsIn() throws Exception {
        assertThat(guard.mayCall(method(Invoices.class, "listInvoices"), Set.of("invoices:read"))).isTrue();
    }

    @Test
    void holdingAnyOneOfSeveralPermissionsIsEnough() throws Exception {
        assertThat(guard.mayCall(method(Invoices.class, "issueCredit"), Set.of("billing:admin"))).isTrue();
        assertThat(guard.mayCall(method(Invoices.class, "issueCredit"), Set.of("invoices:write"))).isTrue();
    }

    @Test
    void anEndpointWithNoPermissionsAnywhereIsPublic() throws Exception {
        assertThat(guard.mayCall(method(Invoices.class, "health"), Set.of())).isTrue();
    }

    @Test
    void aSinglePermissionOnTheClassCoversItsMethods() throws Exception {
        assertThat(guard.mayCall(method(Reports.class, "monthly"), Set.of("reports:read"))).isTrue();
        assertThat(guard.mayCall(method(Reports.class, "monthly"), Set.of("invoices:read"))).isFalse();
    }

    @Test
    void aMethodWithSeveralPermissionsRefusesACallerWithNoneOfThem() throws Exception {
        assertThat(guard.mayCall(method(Invoices.class, "issueCredit"), Set.of("invoices:read"))).isFalse();
    }

    @Test
    void aMethodWithOnePermissionRefusesACallerWithoutIt() throws Exception {
        assertThat(guard.mayCall(method(Invoices.class, "listInvoices"), Set.of("invoices:write"))).isFalse();
    }

    @Test
    void severalPermissionsOnTheClassCoverItsMethods() throws Exception {
        assertThat(guard.mayCall(method(Exports.class, "runExport"), Set.of("billing:admin"))).isTrue();
        assertThat(guard.mayCall(method(Exports.class, "runExport"), Set.of("invoices:read"))).isFalse();
    }
}
