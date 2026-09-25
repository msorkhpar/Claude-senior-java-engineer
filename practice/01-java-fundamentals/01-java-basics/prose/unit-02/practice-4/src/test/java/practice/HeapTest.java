package practice;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HeapTest {

    @Test
    void keepsWhatTheRootsReach() {
        Heap.Obj y = new Heap.Obj("y");
        Heap.Obj x = new Heap.Obj("x").refersTo(y);
        Heap.Obj root = new Heap.Obj("root").refersTo(x);
        Heap.Obj stray = new Heap.Obj("stray");
        Heap.Obj registry = new Heap.Obj("registry");
        Heap.Obj entry = new Heap.Obj("entry");
        registry.refersTo(entry);
        assertThat(Heap.live(List.of(root, x, y, stray, registry, entry), List.of(root, registry)))
                .containsExactlyInAnyOrder("root", "x", "y", "registry", "entry");
    }

    @Test
    void aReachableCycleStillFinishes() {
        Heap.Obj a = new Heap.Obj("a");
        Heap.Obj b = new Heap.Obj("b");
        a.refersTo(b);
        b.refersTo(a);
        Heap.Obj root = new Heap.Obj("root").refersTo(a);
        assertThat(Heap.live(List.of(root, a, b), List.of(root))).containsExactlyInAnyOrder("root", "a", "b");
    }

    @Test
    void anUnreachableCycleIsGarbage() {
        Heap.Obj c = new Heap.Obj("c");
        Heap.Obj d = new Heap.Obj("d");
        c.refersTo(d);
        d.refersTo(c);
        Heap.Obj root = new Heap.Obj("root");
        assertThat(Heap.live(List.of(root, c, d), List.of(root))).containsExactly("root");
    }
}
