The page's violation checks an email address the same way in several places:

```java
public boolean validateForRegistration(String email) {
    return email != null && email.contains("@") && email.contains(".");
}
public boolean validateForPasswordReset(String email) {
    return email != null && email.contains("@") && email.contains(".");
}
```

Every copy is the same piece of knowledge. When one copy is fixed and another is missed,
the forms start to disagree: that is **shotgun surgery**, and DRY's answer is **one
authoritative rule that every caller uses**.

`AccountForms` has three forms: `register`, `resetPassword` and `changeEmail`. Complete it:

- `isValidEmail(String email)` is the default rule: an address is valid when it is not
  null, not blank, and contains both `"@"` and `"."`. It handles `null` itself and returns
  `false`, so no caller has to check for `null` first;
- each of the three forms answers with the class's one rule, the `emailRule` field, and
  adds no check of its own: the rule alone decides. The no-argument constructor sets it to
  `isValidEmail`, and the other constructor sets it to the rule it is given.

Examples:

- `new AccountForms().register("user@example.com")` is `true`, and so is
  `register("first.last@localhost")`, which holds both characters;
  `new AccountForms().changeEmail("userexample.com")` is `false`;
- every form returns `false` for `null`, `""` and `"   "`, and none of them throws;
- with `new AccountForms(e -> e != null && e.endsWith("@corp.example"))`, all three
  forms accept `"ann@corp.example"` and all three reject `"ann@example.com"`: replacing
  the rule in one place changes every form;
- with a rule that accepts everything, `new AccountForms(e -> true)`, every form accepts
  even `null` and `"  "`, and with `e -> e != null && e.startsWith("x")` every form
  accepts `"xyz"`.
