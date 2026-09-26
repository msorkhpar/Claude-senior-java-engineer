A class may declare **nested classes** as components of its own. The page asks you
to weigh their pros and cons. Used well, a nested class is a private building block
nobody outside can see or depend on. Declared `static`, its objects do not each
carry a hidden reference to an outer object.

Write `LinkedStack<T>`, a stack built from linked nodes:

- `push(T value)`, `pop()` (removes and returns the top value), `peek()` (returns the
  top value without removing it), `size()` and `isEmpty()`;
- `pop()` and `peek()` on an empty stack throw `java.util.NoSuchElementException`;
- the node type is **one** nested class of `LinkedStack`, `private` and `static`,
  holding a value and the next node. No other nested class is declared.

**Examples**

```
s.push(1); s.push(2); s.push(3)
s.pop()   -> 3
s.peek()  -> 2
s.size()  -> 2
new LinkedStack<String>().pop() -> NoSuchElementException
```
