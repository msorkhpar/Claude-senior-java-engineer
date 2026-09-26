The course's over-engineered transformer finds the unique words of a text, sorted, through
a pipeline of its own: a `TransformationStage` interface and three stage classes, one to
split, one to remove duplicates and one to sort. The page's advice is to favour the
standard library: `String.split`, and a stream's `distinct()` and `sorted()`, do the same
job in one expression.

Write `of(String input)` in `UniqueWords`. It returns the distinct words of `input`, sorted:

- words are separated by any run of whitespace (spaces, tabs, line breaks), and whitespace
  around the text is ignored: `of("  banana apple  ")` is `["apple", "banana"]`;
- a word is kept exactly as written, case and punctuation included, and words are compared
  and sorted as plain `String`s (natural order, so capitals come first):
  `of("Banana apple banana don't")` is `["Banana", "apple", "banana", "don't"]`;
- each word appears once: `of("banana apple banana")` is `["apple", "banana"]`;
- `of("cherry apple banana")` is `["apple", "banana", "cherry"]`;
- **null or blank input gives an empty list, never `null`**: `of(null)` and `of("   ")`
  are `[]`.
