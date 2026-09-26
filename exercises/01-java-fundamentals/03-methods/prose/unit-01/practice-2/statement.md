A parameter with the same name as a field shadows the field: inside the method, `owner`
means the parameter. `this.owner` reaches the field. Writing `owner = owner;` assigns the
parameter to itself and leaves the field untouched.

Complete `Account`, whose constructor and setter both take a parameter named `owner`:

- `new Account(owner)` stores the owner, trimmed of surrounding spaces;
- `owner()` returns it;
- `rename(owner)` replaces it, trimmed, and returns the old owner;
- a `null` or blank owner is refused with `IllegalArgumentException`, in the constructor and
  in `rename` alike, and a refused rename leaves the owner as it was.

Example: `new Account(" Ada ")` has owner `"Ada"`; `rename("Grace")` returns `"Ada"`, and
the owner is then `"Grace"`.
