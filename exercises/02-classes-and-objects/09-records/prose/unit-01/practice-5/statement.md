Records are only **shallowly** immutable: the component fields are final, but
an object a component refers to, such as a `List`, can still change. In

```java
record Team(String name, List<String> members)
```

a caller could keep the list it passed in and add to it later, or call
`team.members().add(...)`, and the team would change.

Make `Team` really immutable:

- in the compact constructor, store a defensive, unmodifiable copy of
  `members` (for example with `List.copyOf`);
- the list `members()` returns must therefore refuse modification
  (`UnsupportedOperationException`);
- add `Team withMember(String member)`, which returns a **new** team with the
  member appended and leaves this team as it was.

## Examples

```
List<String> names = new ArrayList<>(List.of("Ann", "Ben"));
Team team = new Team("Core", names);
names.add("Cid");                 -> team.members() is still [Ann, Ben]
team.members().add("Dee")         -> UnsupportedOperationException
team.withMember("Eve")            -> Team[name=Core, members=[Ann, Ben, Eve]]; team.members() is still [Ann, Ben]
```
