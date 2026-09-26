package practice;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MethodFinderTest {

    static class Base {
        private String secret() {
            return "base";
        }
    }

    public static class MathOps extends Base {
        public int add(int a, int b) {
            return a + b;
        }

        public double add(double a, double b) {
            return a + b;
        }

        private long add(long a, long b) {
            return a + b;
        }

        public int scale(int a) {
            return a * 10;
        }

        public double scale(double a) {
            return a * 10;
        }
    }

    @Test
    void findsAMethodAndListsPublicOverloads() {
        Method add = MethodFinder.find(MathOps.class, "add", int.class, int.class).orElseThrow();

        assertThat(add.getReturnType()).isSameAs(int.class);
        assertThat(MethodFinder.find(MathOps.class, "subtract", int.class, int.class)).isEmpty();
        assertThat(MethodFinder.overloads(MathOps.class, "scale")).containsExactly("scale(double)", "scale(int)");
    }

    @Test
    void wrapperTypesDoNotFindAPrimitiveMethod() {
        assertThat(MethodFinder.find(MathOps.class, "add", Integer.class, Integer.class)).isEmpty();
        assertThat(MethodFinder.find(MathOps.class, "add", Double.class, Double.class)).isEmpty();
    }

    @Test
    void aPrivateMethodOfASuperclassIsFound() {
        Method secret = MethodFinder.find(MathOps.class, "secret").orElseThrow();

        assertThat(secret.getDeclaringClass()).isSameAs(Base.class);
    }

    @Test
    void overloadsListEveryAccessLevel() {
        assertThat(MethodFinder.overloads(MathOps.class, "add"))
                .containsExactly("add(double,double)", "add(int,int)", "add(long,long)");
    }
}
