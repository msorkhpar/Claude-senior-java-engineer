Spring injects a private field with `field.setAccessible(true)` and
`field.set(target, bean)`, for every field that **carries `@Autowired`**. The
annotation must be kept at run time (`RetentionPolicy.RUNTIME`) for
`isAnnotationPresent` to see it.

Write `Injector.inject(target, beans)` for the nested annotation
`Injector.Inject` (given):

- Every field of `target` that carries `@Inject`, declared on its class **or
  on any superclass**, is set to `beans.get(field's type)`.
- Fields **without `@Inject` are left alone**, whatever their type.
- An `@Inject` field with **no bean** for its type fails with an
  `IllegalStateException` whose message contains the **field's name**.

| target | beans | after `inject` |
|---|---|---|
| `UserService { @Inject private Repository repository; private Repository backup; }` | `{Repository: repo}` | `repository` is `repo`, `backup` is `null` |
| `AuditedService extends UserService { @Inject private Auditor auditor; }` | `{Repository: repo, Auditor: auditor}` | both set |
| `UserService` | `{}` | `IllegalStateException` naming `repository` |
