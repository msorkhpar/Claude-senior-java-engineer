The page's third interview question shows this code, and asks what is wrong with it:

```java
if (score >= 90)
    System.out.println("A");
    System.out.println("Excellent!");
if (score >= 80)
    System.out.println("B");
if (score >= 70)
    System.out.println("C");
else
    System.out.println("Needs improvement");
```

Without braces only the first statement belongs to an `if`, so `"Excellent!"` prints for
every score. The three `if`s are separate, so one score can print several letters, and the
`else` belongs only to the last `if`.

Write `lines(int score)` in `ReportCard`. It returns, in order, the lines the corrected
code prints:

- `90` and above: `["A", "Excellent!"]`;
- `80` to `89`: `["B"]`;
- `70` to `79`: `["C"]`;
- below `70`: `["Needs improvement"]`.

Examples: `lines(95)` is `["A", "Excellent!"]`, `lines(85)` is `["B"]`, and `lines(10)`
is `["Needs improvement"]`.
