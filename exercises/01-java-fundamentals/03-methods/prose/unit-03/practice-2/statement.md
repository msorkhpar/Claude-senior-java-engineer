A method that changes an object it was passed changes the caller's object. When the caller
did not ask for that, it is a bug that shows up far from the method. A method that only
needs to read its input works on a copy.

Write `ranked(int[] scores)` in `Ranking`. It returns a new array holding the scores from
highest to lowest, and leaves the caller's array exactly as it was:

- `ranked(new int[]{70, 95, 80})` is `{95, 80, 70}`, and the caller's array is still
  `{70, 95, 80}`;
- `ranked(new int[]{})` is `{}`.
