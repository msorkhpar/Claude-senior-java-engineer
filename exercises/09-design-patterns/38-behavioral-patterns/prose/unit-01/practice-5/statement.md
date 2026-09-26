The page builds complex strategies out of simple ones: `andThen()` chains
functions, and a pipeline applies a list of strategy stages one after the
other. Each stage is a `Function<List<T>, List<T>>`.

Write `DataPipeline<T>`:

- `addStage(stage)` **adds the stage to this pipeline and returns this same
  pipeline**, so calls can be chained. A `null` stage is **refused when it is
  added**, with `IllegalArgumentException`.
- `execute(input)` starts from a copy of `input` and gives each stage the
  previous stage's answer, **in the order the stages were added**. It returns
  the last answer, and **never the caller's list itself**, even with no stages.
  `null` input throws `IllegalArgumentException`.
- `stageCount()` returns how many stages were added.

| stages | input | answer |
|---|---|---|
| keep `n > 0`, distinct, sorted | `[3, -1, 2, 3, 0, 5]` | `[2, 3, 5]` |
| keep the first two, then sort | `[5, 1, 4]` | `[1, 5]` |
| sort, then keep the first two | `[5, 1, 4]` | `[1, 4]` |
| none | `[7, 8]` | a new list `[7, 8]` |
