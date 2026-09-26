A pipeline composed of independent stages needs no change to process many items at once:
each item flows through it on its own virtual thread. The page's example:

```java
var pipeline = new ProcessingPipeline<String>()
    .addStage(String::trim)
    .addStage(String::toUpperCase)
    .addStage(s -> s + " [PROCESSED]");

var executor = new ConcurrentPipelineExecutor<>(pipeline);
List<String> results = executor.processAll(
    List.of("  hello  ", "  world  ", "  java  ")
);
// Results: ["HELLO [PROCESSED]", "WORLD [PROCESSED]", "JAVA [PROCESSED]"]
```

`addStage` is given. Write `execute` and `processAll` in `Pipelines`:

- `execute(input)` runs `input` through every stage in order; a pipeline with no stages
  returns its input unchanged;
- a stage that returns `null` **stops the pipeline**: `execute` returns `null` at once and
  no later stage is called. In `processAll` such an item keeps its place as `null`:
  a pipeline that stops `"drop"` turns `["a", "drop", "b"]` into `["A", null, "B"]`;
- `processAll(items)` runs the items at the same time, each through the pipeline on **its
  own virtual thread** (`Executors.newVirtualThreadPerTaskExecutor()`), and returns the results **in the order
  of the items**, whichever finishes first. An empty list gives an empty list.
