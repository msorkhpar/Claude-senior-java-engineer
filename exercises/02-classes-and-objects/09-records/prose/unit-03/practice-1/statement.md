Records are ideal **data transfer objects**: a DTO carries data between the
layers of an application, and a record's compact constructor guards every way
a DTO can be made, a factory method included.

`PersonDTO(String name, int age, String email)` is the course's DTO. Its
compact constructor already validates (a null or empty name, a negative age, an
email that is null or has no `@` are refused with `IllegalArgumentException`),
and it has `isAdult()`. Write its static factory

```java
static PersonDTO parse(String line)
```

which reads one line of the form `name,age,email`:

- strip the spaces around each field;
- a line that does not have exactly three fields is refused with an
  `IllegalArgumentException`;
- an age that is not a whole number is refused with an
  `IllegalArgumentException` (not a `NumberFormatException`);
- build the DTO through its canonical constructor, so its own checks apply
  (a parsed line with a bad email is refused with `Invalid email format`).

## Examples

```
parse("John Doe,30,john@example.com")         -> PersonDTO[name=John Doe, age=30, email=john@example.com]
parse(" Jane Doe , 15 , jane@example.com ")   -> PersonDTO[name=Jane Doe, age=15, email=jane@example.com]
parse("John Doe,30")                          -> IllegalArgumentException
parse("John Doe,thirty,john@example.com")     -> IllegalArgumentException
parse("John Doe,30,invalidemail")             -> IllegalArgumentException: Invalid email format
```
