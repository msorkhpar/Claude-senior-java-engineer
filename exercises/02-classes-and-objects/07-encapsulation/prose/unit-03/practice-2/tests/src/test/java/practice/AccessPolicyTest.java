package practice;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AccessPolicyTest {

    /** A fresh, mutable table for each test, built the way a config loader builds it. */
    private static Map<String, List<String>> table() {
        Map<String, List<String>> t = new LinkedHashMap<>();
        t.put("auditor", new ArrayList<>(List.of("read-ledger", "export-csv")));
        t.put("on-call", new ArrayList<>(List.of("restart-service", "read-logs", "page-team")));
        return t;
    }

    @Test
    void answersFromTheTableItWasBuiltFrom() {
        AccessPolicy policy = new AccessPolicy(table());

        assertThat(policy.allows("auditor", "export-csv")).isTrue();
        assertThat(policy.allows("on-call", "page-team")).isTrue();
        assertThat(policy.allows("auditor", "restart-service")).isFalse();
        assertThat(policy.allows("intern", "read-ledger")).isFalse();
        assertThat(policy.roles()).containsExactlyInAnyOrder("auditor", "on-call");
    }

    @Test
    void listsEachRolesActionsInTheOrderGiven() {
        AccessPolicy policy = new AccessPolicy(table());

        assertThat(policy.actionsFor("on-call")).containsExactly("restart-service", "read-logs", "page-team");
        assertThat(policy.actionsFor("intern")).isEmpty();
    }

    @Test
    void theCallersListsCanChangeWithoutChangingThePolicy() {
        Map<String, List<String>> source = table();
        AccessPolicy policy = new AccessPolicy(source);

        source.get("auditor").add("delete-ledger");          // the loader reuses its lists
        source.get("on-call").clear();

        assertThat(policy.allows("auditor", "delete-ledger")).isFalse();
        assertThat(policy.actionsFor("on-call")).containsExactly("restart-service", "read-logs", "page-team");
    }

    @Test
    void noCallerCanAddAnActionThroughWhatThePolicyReturns() {
        AccessPolicy policy = new AccessPolicy(table());

        try {
            policy.actionsFor("auditor").add("delete-ledger");
        } catch (UnsupportedOperationException refused) {
            // a list nobody can change
        }

        assertThat(policy.allows("auditor", "delete-ledger")).isFalse();
    }

    @Test
    void noCallerCanRemoveARoleThroughWhatThePolicyReturns() {
        AccessPolicy policy = new AccessPolicy(table());

        try {
            policy.roles().remove("on-call");
        } catch (UnsupportedOperationException refused) {
            // a set nobody can change
        }

        assertThat(policy.allows("on-call", "restart-service")).isTrue();
    }

    @Test
    void noSubclassCanChangeHowAPolicyAnswers() {
        boolean classIsFinal = Modifier.isFinal(AccessPolicy.class.getModifiers());
        boolean everyAnswerIsFinal = true;
        for (Method m : AccessPolicy.class.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) && !Modifier.isStatic(mod) && !Modifier.isFinal(mod)) everyAnswerIsFinal = false;
        }

        assertThat(classIsFinal || everyAnswerIsFinal)
                .as("a subclass can override allows(), actionsFor() or roles()")
                .isTrue();
    }
}
