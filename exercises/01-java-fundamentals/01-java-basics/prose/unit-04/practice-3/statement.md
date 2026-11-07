The billing service keeps each tenant's 48 feature switches in one `long`: feature n is on when
bit n is set. Support has three tickets open. A tenant who bought the audit log (feature 42) also
got single sign-on (feature 10). A cleanup job that switches off every trial feature switched
some of them back on. And a tenant with only one feature of the "analytics" bundle was shown the
whole bundle.

`FeatureMask` already builds sets of features and switches them on. Finish the three `TODO`
methods in `src/main/java/practice/FeatureMask.java`:

- `bit(feature)`: the set that holds one feature;
- `disable(mask, features)`: the mask with those features switched off;
- `hasAll(mask, features)`: whether every feature of the set is on.

About three lines in total.
