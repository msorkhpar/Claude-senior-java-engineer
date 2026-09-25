The JVM does not count references. Its garbage collector starts from the **GC
roots** (local variables of live threads, static fields and a few more) and
keeps every object it can **reach** by following references. Everything else is
garbage, however many references point at it.

`Heap.Obj` is an object with a name and a list of references to other objects.
Write `Heap.live(List<Obj> heap, List<Obj> roots)`. It returns the names of the
objects in `heap` that are reachable from `roots`: the roots themselves, and every
object reachable from one of them through any chain of references.

- A static registry that keeps adding objects is a **root**: everything in it
  stays alive, which is how a memory leak happens in a garbage-collected language.
- References can form **cycles** (`a` refers to `b`, `b` refers to `a`). Your
  answer must still finish.
- A cycle nothing reachable refers to is garbage, even though each of its
  objects is referred to.

`Obj` does not override `equals` or `hashCode`, so a `HashSet<Obj>` tracks each
object by identity.
