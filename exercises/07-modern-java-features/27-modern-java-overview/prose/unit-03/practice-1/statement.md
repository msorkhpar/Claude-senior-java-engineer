The search team built its query library on Java 22 and tried out a preview feature in one class.
Three other services use the library, and the platform team now plans to move all of them to
Java 24. Last time, a service reached production and failed to start with
`UnsupportedClassVersionError`. This time a check runs over every jar first and lists the classes
the target runtime would refuse.

`ClassFileCheck` already reads the bytes of each class file and builds the report. Finish the three
`TODO` methods in `src/main/java/practice/ClassFileCheck.java`:

- `release(...)`: the Java feature release a class was compiled for;
- `usesPreview(...)`: whether it was compiled with preview features enabled;
- `loads(...)`: whether a runtime of a given release, started with or without `--enable-preview`,
  accepts it.

`u2(...)`, `minorVersion(...)` and `majorVersion(...)` read the header for you. About six lines in
total.
