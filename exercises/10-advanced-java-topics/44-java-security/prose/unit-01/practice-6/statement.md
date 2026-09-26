Path traversal is a user sending `../../etc/passwd` where a file name is
expected. The page's defence: **resolve** the name against a known base
directory, **normalize** it (removing `.` and `..`), and check that the result
still **starts with the base**, using `Path.startsWith`, which compares whole
name elements. `normalize()` does not follow symbolic links, so for a path
that **exists**, the check is repeated on the **real paths**
(`toRealPath()`).

Write `SafePaths.resolve(base, userSupplied)` that returns the normalized path
inside `base`, or throws `SecurityException`. `base` is an existing directory.

| base | user supplied | result |
|---|---|---|
| `/srv/base` | `reports/2024.txt` | `/srv/base/reports/2024.txt` |
| `/srv/base` | `./a/../b.txt` | `/srv/base/b.txt` |
| `/srv/base` | `../../etc/passwd` | `SecurityException` |
| `/srv/base` | `docs/../../outside.txt` | `SecurityException` |
| `/srv/base` | `../base-secrets/key.txt` | `SecurityException` |
| `/srv/base` | `link` (a symbolic link to `/srv/elsewhere`) | `SecurityException` |
