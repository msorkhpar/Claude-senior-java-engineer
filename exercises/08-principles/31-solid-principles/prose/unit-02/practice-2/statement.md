The page's second pitfall is a validator that is edited for every new rule:

```java
class Validator {
    boolean isValid(String s) {
        return s != null && !s.isEmpty() && s.length() >= 5; // keep adding conditions
    }
}
```

Its fix is the Decorator pattern: **each decorator implements the same `Validator`
interface as the validator it wraps, asks that validator first, and adds its own rule**.
No validator is ever edited to add a rule. In `Checks`, write:

- `NonNullValidator`: valid when the value is not null;
- `NonEmptyValidator(delegate)`: valid when the delegate says valid and the value is not
  empty (`"  "` is not empty);
- `MinLengthValidator(delegate, min)`: valid when the delegate says valid and the value
  has at least `min` characters, spaces included (`value.length()`, nothing trimmed).

Each decorator **always asks its delegate first**, with the same value, and only then
applies its own rule.

The page composes them as

```java
Validator<String> validator = new MinLengthValidator(
    new NonEmptyValidator(new NonNullValidator()), 5);
```

which rejects `null`, `""` and `"abc"` and accepts `"hello"`. A decorator wraps **any**
`Validator<String>`, including a lambda written later, so
`new MinLengthValidator(v -> v != null && v.startsWith("A"), 3)` accepts `"Anna"` and
rejects `"Bob12"`.
