package practice;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TxTest {

    @Tx.Transactional(readOnly = true)
    public static class Repo {
        @Tx.Transactional
        public void save() {
        }

        public void find() {
        }
    }

    public static class CachedRepo extends Repo {
        @Override
        public void find() {
        }
    }

    public static class Plain {
        @Tx.Transactional
        public void save() {
        }
    }

    public static class SubPlain extends Plain {
        @Override
        public void save() {
        }
    }

    @Test
    void theMethodAnnotationWinsOverTheClass() {
        assertThat(Tx.mode(Repo.class, "save")).contains("read-write");
        assertThat(Tx.mode(Repo.class, "find")).contains("read-only");
        assertThat(Tx.mode(Plain.class, "save")).contains("read-write");
    }

    @Test
    void anOverrideDropsTheMethodAnnotation() {
        assertThat(Tx.mode(SubPlain.class, "save")).isEmpty();
    }

    @Test
    void theClassAnnotationReachesSubclasses() {
        assertThat(Tx.mode(CachedRepo.class, "find")).contains("read-only");
    }
}
