The platform team's `Interceptors` registry runs an action before every method that carries a
given annotation: services register `@Idempotent` to attach an idempotency key, `@Audited` to
write an audit row, and so on. Twice this quarter a service registered its own annotation, saw no
error, and shipped with the action never running.

`register` should refuse, at start-up, any annotation type the registry could never find on a
method, and say why. It already does once you finish the two `TODO` methods in
`src/main/java/practice/Interceptors.java`:

- `policyOf(type)`: the retention policy that applies to annotations of that type;
- `visibleToReflection(policy)`: whether annotations kept under that policy can be found on a
  method while the program runs.

About four lines in total. The tests register annotation types of their own and the JDK's.
