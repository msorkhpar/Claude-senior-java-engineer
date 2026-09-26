package practice;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InjectorTest {

    interface Repository {
        String find();
    }

    static class SqlRepository implements Repository {
        @Override
        public String find() {
            return "User found";
        }
    }

    static class Auditor {
    }

    static class UserService {
        @Injector.Inject
        private Repository repository;

        private Repository backup;

        String findUser() {
            return repository.find();
        }
    }

    static class AuditedService extends UserService {
        @Injector.Inject
        private Auditor auditor;
    }

    @Test
    void injectsAnAnnotatedPrivateField() {
        Repository repo = new SqlRepository();
        UserService service = new UserService();

        Injector.inject(service, Map.of(Repository.class, repo));

        assertThat(service.repository).isSameAs(repo);
        assertThat(service.findUser()).isEqualTo("User found");
    }

    @Test
    void unannotatedFieldsAreLeftAlone() {
        UserService service = new UserService();

        Injector.inject(service, Map.of(Repository.class, new SqlRepository()));

        assertThat(service.backup).isNull();
    }

    @Test
    void inheritedFieldsAreInjected() {
        Repository repo = new SqlRepository();
        Auditor auditor = new Auditor();
        AuditedService service = new AuditedService();

        Injector.inject(service, Map.of(Repository.class, repo, Auditor.class, auditor));

        assertThat(service.auditor).isSameAs(auditor);
        assertThat(((UserService) service).repository).isSameAs(repo);
    }

    @Test
    void aMissingBeanIsRefused() {
        assertThatThrownBy(() -> Injector.inject(new UserService(), Map.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("repository");
    }
}
