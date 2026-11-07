# 10.6.2. Common Vulnerabilities

A security review of an order service files four findings, each a request that worked against the
test environment. A `sort` parameter ordered the product list by the digits of a staff PIN, one digit
per request. A download of `../config/db.properties` returned the database settings. An uploaded XML
order came back with the contents of a server file in its `note` field. A session cookie decoded into
a class that sessions never hold, and that class's code ran.

Every run below attacks code written for it, in one JVM: an H2 in-memory database, files in a
directory beside the program, and the JDK's own XML parser and `ObjectInputStream`. Input validation, `SecureRandom` and secrets are unit 10.6.1; encryption and
password hashing are unit 10.6.3.

## A Bind Parameter Holds a Value, So a Sort Column Comes from an Allow-List

Unit 7.4.5 shows a `WHERE` value pasted into SQL and the fix, a `?` bound with `setString`. A
product list that the user can sort has a part a `?` can't hold: the column after `ORDER BY`. The
list endpoint pastes it:

```java
static List<String> listProducts(String sort) throws SQLException {
    String sql = "SELECT name FROM product ORDER BY " + sort;
    List<String> names = new ArrayList<>();
    try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
        while (rs.next()) names.add(rs.getString(1));
    }
    return names;
}
```

The table is `product(name, price, cost, added_on)`; lamp costs 30, chair 45, desk 120. The
database also holds `staff(login, pin)` with one row, PIN `7351`. The reviewer sent `price`,
then a `CASE` expression with a different guess for the PIN's first digit each time:

```java
String probe = "CASE WHEN (SELECT SUBSTRING(pin, 1, 1) FROM staff) = '" + d + "' THEN price ELSE -price END";
```

```text
price      [lamp, chair, desk]
probe '6'  [desk, chair, lamp]
probe '7'  [lamp, chair, desk]
probe '8'  [desk, chair, lamp]
```

A wrong guess sorts by `-price`, the right one by `price`, so the order of a list that shows no PIN
tells the caller whether the guess was right. Nine requests per digit at most read the PIN. H2 also
runs two statements in one `executeQuery`: `price; DROP TABLE staff` returned the sorted list, and
the next query on `staff` failed with `Table "STAFF" not found`.

Binding the column doesn't work, because a parameter is a value, never a name. `ORDER BY ?` on H2,
with `setString(1, "price")` and then `setString(1, "2")`:

```text
price  Data conversion error converting "price"; SQL statement:
2  Order by expression "2" must be in the result list in this case; SQL statement:
```

H2 reads a parameter in `ORDER BY` as a column number, and the query selects one column, so `2`
is out of range. Another database may accept the statement and sort every row by the same
constant, so the list comes back unsorted with no error.

The column has to be in the SQL text, so the code decides what that text is. Two checks, by shape
and by lookup:

```java
static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

static String byShape(String sort) {
    if (!IDENTIFIER.matcher(sort).matches()) throw new IllegalArgumentException("bad sort");
    return sort;
}

static final Map<String, String> SORT_COLUMNS = Map.of(
        "name", "name",
        "price", "price",
        "newest", "added_on DESC");

static String byAllowList(String sort) {
    String column = SORT_COLUMNS.get(sort);
    if (column == null) throw new IllegalArgumentException("bad sort");
    return column;
}
```

```text
sort                       by shape               allow-list
price                      [lamp, chair, desk]    [lamp, chair, desk]
newest                     SQLException           [desk, lamp, chair]
cost                       [chair, lamp, desk]    refused
CASE WHEN (...)            refused                refused
price; DROP TABLE staff    refused                refused
```

The shape check stops the expressions, but `cost`, what the shop pays its supplier, is a real column
of the table, and the API now sorts by it. The allow-list maps a public key to SQL text the code
wrote itself, so no character the user typed reaches the SQL, a key can differ from its column
(`newest`), and a column the API doesn't expose can't be reached. A sort direction gets
the same treatment: map `asc` and `desc` to `ASC` and `DESC`, and never append the input.

<!-- practice 1 -->

## A `..` Escapes the Base Unless the Check Normalizes the Path and Compares Elements

The download endpoint serves files from `files/uploads`. Beside it are `files/config` and
`files/uploads-private`, and `uploads/team` is a symbolic link to `../config` that someone made for
sharing. Four checks, each given the name from the request:

```java
static Path notNormalized(String name) {
    Path p = base.resolve(name);
    if (!p.startsWith(base)) throw new SecurityException();
    return p;
}

static Path stringPrefix(String name) {
    Path p = base.resolve(name).normalize();
    if (!p.toString().startsWith(base.toString())) throw new SecurityException();
    return p;
}

static Path normalized(String name) {
    Path p = base.resolve(name).normalize();
    if (!p.startsWith(base)) throw new SecurityException();
    return p;
}

static Path real(String name) throws IOException {
    Path realBase = base.toRealPath();
    Path p = base.resolve(name).toRealPath();
    if (!p.startsWith(realBase)) throw new SecurityException();
    return p;
}
```

`base` is the absolute path of `files/uploads`. What each check let `Files.readString` read:

```text
name                            no normalize          string prefix         normalize             toRealPath
invoice-17.txt                  invoice 17            invoice 17            invoice 17            invoice 17
../config/db.properties         password=placeholder  refused               refused               refused
../uploads-private/payroll.txt  payroll               payroll               refused               refused
team/db.properties              password=placeholder  password=placeholder  password=placeholder  refused
```

`resolve` only appends the name, so `uploads/../config/db.properties` still begins with the
elements of `uploads`, and `Path.startsWith`, which compares elements, says yes. The file system
then follows the `..`.

The string prefix check normalizes, but compares characters: `files/uploads-private/payroll.txt`
begins with the characters `files/uploads`. `Path.startsWith` compares whole elements, and
`uploads-private` is not `uploads`.

Refusing names that contain `..` is a deny-list, with the problem unit 10.6.1 shows: one decoding
layer later, `%2e%2e%2f` is `../`. Checking the resolved path, not the name, catches every spelling
of `..`.

### A Symlink Inside the Base Points Anywhere; Check the Real Path

`normalize()` works on the text of the path and never reads the disk: `uploads/team/db.properties` has no `..`, so as text it stays inside `uploads`, while
the file system follows `team` to `config`. `toRealPath()` asks the file system: it follows every
link and returns the path the file really has, which starts with `files/config`.

Text and disk can also disagree about `..` itself. With `files/secret.txt` present, the name
`team/../secret.txt` normalizes to `uploads/secret.txt`, which passes the check and doesn't exist,
while the file system follows `team` to `config` and then `..` to `files`, and `Files.readString`
returned the contents of `files/secret.txt`. The `Path.normalize()` javadoc warns about exactly
this. Only the real path says where a name leads.

`toRealPath()` throws `NoSuchFileException` for a file that doesn't exist, so code that creates a
file checks the real path of its parent directory. Compare with `base.toRealPath()`, not `base`:
with the base reached through a link, `files/uploads-link`, the real path of `invoice-17.txt` failed
`startsWith(base)` and passed `startsWith(base.toRealPath())`.

<!-- practice 2 -->

## The JDK's XML Parsers Read External Entities Until You Turn Them Off

The import endpoint parses partner orders with `DocumentBuilderFactory.newInstance()` and reads
the `note` element. The reviewer's order declares an entity whose value is a file:

```xml
<?xml version="1.0"?>
<!DOCTYPE order [
  <!ENTITY note SYSTEM "secret.txt">
]>
<order><item>lamp</item><note>&note;</note></order>
```

`order-param.xml` does the same through a parameter entity: `<!ENTITY % ext SYSTEM "entities.dtd">`
then `%ext;`, and the file `entities.dtd` declares `<!ENTITY note "the dtd was read">`. Each document
was parsed by a factory with one setting changed, and the run printed the `note` it produced:

```java
setups.put("default", f -> {});
setups.put("setExpandEntityReferences(false)", f -> f.setExpandEntityReferences(false));
setups.put("external-general-entities false",
        f -> f.setFeature("http://xml.org/sax/features/external-general-entities", false));
setups.put("disallow-doctype-decl true",
        f -> f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true));
setups.put("ACCESS_EXTERNAL_DTD \"\"", f -> f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, ""));
```

```text
factory                            order.xml                  order-param.xml
default                            "contents of secret.txt"   "the dtd was read"
setExpandEntityReferences(false)   ""                         ""
external-general-entities false    ""                         "the dtd was read"
disallow-doctype-decl true         refused: DOCTYPE           refused: DOCTYPE
ACCESS_EXTERNAL_DTD ""             refused: file access       refused: file access
```

On Java 21 the default factory reads a file named by the document and puts its text into the
result. `SAXParserFactory`, `XMLInputFactory` and an identity `Transformer` read `secret.txt` from the
same document too. A `SYSTEM` identifier is a URI, so it can also name an `http:` address that only
the server can reach.

An empty note doesn't mean nothing was read. `external-general-entities` turns off one kind of
entity, and the parameter entity still read its file. With `setExpandEntityReferences(false)`, a copy
of `order-param.xml` whose parameter entity names a missing `.dtd` failed with
`FileNotFoundException`: the parser still opened the file.

Two settings refused both documents, with these messages:

```text
SAXParseException: DOCTYPE is disallowed when the feature "http://apache.org/xml/features/disallow-doctype-decl" set to true.
SAXParseException: External Entity: Failed to read external document 'secret.txt', because 'file' access is not allowed due to restriction set by the accessExternalDTD property.
```

`disallow-doctype-decl` refuses any document with a `DOCTYPE`, which suits a format that never
needs one. `ACCESS_EXTERNAL_DTD` set to `""` allows no protocol for external DTDs and entities, and
a document whose `DOCTYPE` declares only internal entities still parsed. Each factory class is
configured on its own, and the names differ: `XMLInputFactory` rejects `disallow-doctype-decl` with
`IllegalArgumentException`, and takes `SUPPORT_DTD` set to `false` and `ACCESS_EXTERNAL_DTD`
instead. Setting `FEATURE_SECURE_PROCESSING` to `true` on the DOM factory refused both documents
too.

<!-- practice 3 -->

## Check Classes With a Filter Before readObject Builds Them, Not the Result After

Sessions are stored as serialized `Preferences`. `CacheWarmer` stands in for any other
`Serializable` class on the class path; its `readObject` prints a line, where a real class might
open a connection or write a file:

```java
record Preferences(String theme, int pageSize) implements Serializable {}

/** Harmless, but not a class a session should ever contain. */
final class CacheWarmer implements Serializable {
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        System.out.println("  CacheWarmer.readObject ran");
    }
}

record Envelope(String kind, Object payload) implements Serializable {}
```

`Envelope` is a session wrapper whose payload can be any object. The session code casts the result,
`(Preferences) read(data, filter)`:

```java
static final ObjectInputFilter ONLY_PREFERENCES =
        ObjectInputFilter.Config.createFilter("Preferences;Envelope;!*");

static Object read(byte[] data, ObjectInputFilter filter) throws IOException, ClassNotFoundException {
    try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data))) {
        if (filter != null) in.setObjectInputFilter(filter);
        return in.readObject();
    }
}
```

```text
CacheWarmer, no filter:
  CacheWarmer.readObject ran
  java.lang.ClassCastException: class CacheWarmer cannot be cast to class Preferences (CacheWarmer and Preferences are in unnamed module of loader 'app')
CacheWarmer, filter:
  java.io.InvalidClassException: filter status: REJECTED
Envelope holding a CacheWarmer, filter:
  java.io.InvalidClassException: filter status: REJECTED
```

Without a filter the cast failed, but only after `CacheWarmer`'s code had run: `readObject` builds
the whole graph, calling each class's own `readObject`, before it returns anything to cast. The
filter is asked about each class as the stream meets it, nested ones too (`Envelope` passed and its
payload was refused), and the refused class's `readObject` never ran.

The pattern ends in `!*` because the stream allows any class its filter answers `UNDECIDED` for: a
filter answering `UNDECIDED` for everything let `CacheWarmer.readObject` run. The JVM-wide filter is the system property
`jdk.serialFilter`: started with `-Djdk.serialFilter='Preferences;!*'`, the same unfiltered read
threw `InvalidClassException`.

The filter checks classes, not the values inside them, and this pattern sets none of its size
limits (`maxdepth`, `maxrefs`, `maxbytes`, `maxarray`). Data that crosses a trust boundary is safer
in a format that builds only the types you name, such as JSON mapped onto records.

<!-- practice 4 -->

<!-- practice 5 -->

## Summary

- **A bind parameter holds a value, so a sort column comes from an allow-list.** A pasted `ORDER BY`
  leaked a PIN through the order of the rows and ran a second statement; an identifier-shaped check
  still sorted by a private column. Map public keys to SQL text the code wrote.
- **A `..` escapes the base unless the check normalizes the path and compares elements.** Without
  normalizing, `startsWith` passed `../`; a string prefix passed a sibling directory. A symlink inside
  the base passes both, and makes `normalize()` itself point at the wrong file; compare
  `toRealPath()` with the base's real path.
- **The JDK's XML parsers read external entities until you turn them off.** Set
  `disallow-doctype-decl`, or `ACCESS_EXTERNAL_DTD` to `""`; turning off external general entities
  left a parameter entity reading its file.
- **Check classes with a filter before `readObject` builds them.** A cast afterwards runs the
  unexpected class's code first; an allow-list filter ending in `!*` refused it at any depth.

## Reference

| Call or setting (Java 21) | Behaviour | Source |
|---|---|---|
| `PreparedStatement` parameter | a value; `ORDER BY ?` on H2 is read as a column number | run |
| H2 `Statement.executeQuery("...; DROP TABLE t")` | runs both statements | run |
| `Path.resolve(other)` | appends `other`; returns `other` when it is absolute | javadoc |
| `Path.normalize()` | removes `.` and `name/..` from the text; no file system access, so `link/..` resolves differently on disk | javadoc, run |
| `Path.startsWith(Path)` | compares name elements, not characters | javadoc, run |
| `Path.toRealPath()` | follows symlinks; `NoSuchFileException` if the file is missing | javadoc, run |
| `DocumentBuilderFactory`, `SAXParserFactory`, `XMLInputFactory`, `TransformerFactory` defaults | read a file named by an external entity | run |
| `http://apache.org/xml/features/disallow-doctype-decl` | any `DOCTYPE` is a fatal error | run |
| `XMLConstants.ACCESS_EXTERNAL_DTD` = `""` | no protocol allowed for external DTDs and entities; internal entities still work | run, javadoc |
| `XMLConstants.FEATURE_SECURE_PROCESSING` set to `true` on the factory | refused both documents | run |
| `XMLInputFactory` | rejects `disallow-doctype-decl`; accepts `SUPPORT_DTD` and `ACCESS_EXTERNAL_DTD` | run |
| `ObjectInputFilter.Config.createFilter("a.B;!*")` | allows `a.B`, rejects the rest | javadoc, run |
| a filter returning `UNDECIDED` | the class is read | run |
| `-Djdk.serialFilter=...` | the JVM-wide filter for streams without their own | javadoc, run |

## Challenges

1. Your report endpoint lets users choose the sort column and the columns shown. Design the
   allow-list for both. What changes when an admin can add custom columns at run time?
2. A batch job writes files into a directory that users choose by name. Which check works when the
   file doesn't exist yet, and what can another process do between your check and the write?
3. Find every place in a service that turns bytes into objects: `ObjectInputStream`, XML, JSON with
   polymorphic types, a cache client. Which of them would accept a class you never meant to
   receive?

## Design Note: Why the Safe Setting Isn't the Default

The two unsafe defaults on this page are older than the attacks on them. External entities are part
of XML 1.0, and documents that use them still exist. Java serialization was designed to rebuild any graph of
`Serializable` objects, which is what made it convenient. A JDK that changed either default would
break programs that work, so the JDK added switches and left them off.

The sort map and the serial filter are allow-lists, and refusing every `DOCTYPE` is a stricter form
of one. They cost the same: when the format gains a class, a sort key or a construct, someone edits
the list, and a forgotten entry shows up as a refused request. A deny-list fails without a sign
when someone adds a class, a link or an encoding it never named.

## Code Examples

- Source: [CommonVulnerabilities.java](src/main/java/com/github/msorkhpar/claudejavatutor/javasecurity/CommonVulnerabilities.java)
- Test: [CommonVulnerabilitiesTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/javasecurity/CommonVulnerabilitiesTest.java)
