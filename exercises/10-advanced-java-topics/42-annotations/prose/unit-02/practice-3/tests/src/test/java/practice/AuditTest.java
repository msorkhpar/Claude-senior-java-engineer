package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuditTest {

    @Audit.Auditable(level = "DEBUG")
    @Audit.Author("Jane")
    static class ParentService {
    }

    static class ChildService extends ParentService {
    }

    @Audit.Auditable(level = "TRACE")
    static class LoudChild extends ParentService {
    }

    static class Grandchild extends ChildService {
    }

    static class Plain {
    }

    @Audit.Auditable(level = "WARN")
    static class Base {
    }

    @Audit.Auditable(level = "DEBUG")
    static class Middle extends Base {
    }

    static class Leaf extends Middle {
    }

    @Audit.Auditable(level = "ERROR")
    interface Tracked {
    }

    static class Impl implements Tracked {
    }

    @Test
    void describesDeclaredAndInheritedLevels() {
        assertThat(Audit.describe(ParentService.class)).isEqualTo("DEBUG declared");
        assertThat(Audit.describe(ChildService.class)).isEqualTo("DEBUG from ParentService");
        assertThat(Audit.describe(LoudChild.class)).isEqualTo("TRACE declared");
        assertThat(Audit.describe(Grandchild.class)).isEqualTo("DEBUG from ParentService");
        assertThat(Audit.describe(Plain.class)).isEqualTo("none");
        assertThat(Audit.authorOf(ParentService.class)).contains("Jane");
    }

    @Test
    void theNearestAnnotatedSuperclassWins() {
        assertThat(Audit.describe(Leaf.class)).isEqualTo("DEBUG from Middle");
    }

    @Test
    void anInterfaceAnnotationIsNotInherited() {
        assertThat(Audit.describe(Impl.class)).isEqualTo("none");
    }

    @Test
    void onlyAnInheritedAnnotationIsInherited() {
        assertThat(Audit.authorOf(ChildService.class)).isEmpty();
        assertThat(Audit.authorOf(Grandchild.class)).isEmpty();
    }
}
