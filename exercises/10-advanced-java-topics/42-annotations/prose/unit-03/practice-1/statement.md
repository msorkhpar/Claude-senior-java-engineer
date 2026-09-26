An annotation type's retention is itself an annotation, `@Retention`, and it
is RUNTIME-retained, so reflection can read it from the annotation's class
object. The page's key points: **with no `@Retention` the policy is CLASS**,
not RUNTIME, and the stages nest as `SOURCE < CLASS < RUNTIME`, so **a longer
retention includes the shorter stages**.

Write `Retentions`:

- `policyOf(Class<? extends Annotation> type)`: the retention policy of `type`.
- `availableIn(Class<? extends Annotation> type, RetentionPolicy stage)`:
  whether an annotation of `type` still exists at `stage` (`SOURCE` = in the
  source, `CLASS` = in the `.class` file, `RUNTIME` = to reflection).

| type | `policyOf` | `availableIn(.., SOURCE)` | `CLASS` | `RUNTIME` |
|---|---|---|---|---|
| `Deprecated` | `RUNTIME` | `true` | `true` | `true` |
| `Override` | `SOURCE` | `true` | `false` | `false` |
| `@Retention(CLASS) @interface Generated` | `CLASS` | `true` | `true` | `false` |
| `@interface Plain` (no `@Retention`) | `CLASS` | `true` | `true` | `false` |
