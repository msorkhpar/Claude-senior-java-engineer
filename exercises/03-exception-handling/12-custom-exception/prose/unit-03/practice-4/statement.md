Two anti-patterns hide or blur a failure: *swallowing* it in an empty `catch`,
and *logging and rethrowing* it so that every layer logs it again. Log a
failure once, where it is finally handled. Write `Importer(List<String> log)`:

- `int importAll(List<String> records) throws ImportException` parses every
  record as an `int` and returns their sum. At the first record that is not an
  `int` as `Integer.parseInt` reads it (a number out of the `int` range is not
  one) it throws `ImportException("Bad record at line <n>: <record>")`
  (lines count from 1). This layer does **not** log.
- `int importOrZero(List<String> records)` is the handler: it returns
  `importAll(records)`, or, when that throws, appends
  `"import failed: " + message` to the log and returns `0`.

| records | `importAll` | `importOrZero` | log afterwards |
|---|---|---|---|
| `["1", "2", "3"]` | `6` | `6` | `[]` |
| `["1", "x", "3"]` | throws `ImportException("Bad record at line 2: x")` | `0` | `["import failed: Bad record at line 2: x"]` |

Records with surrounding spaces are not among the inputs. `ImportException` is given. Whoever investigates the failure later wants to see
the parse error behind it too.
