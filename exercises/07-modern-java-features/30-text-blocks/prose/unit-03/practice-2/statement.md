The pipeline file of the monorepo is generated: each step runs a shell script kept in `ci/`, and
the generator copies the script into the step's YAML `run: |` block. The generated file is checked
in and compared byte for byte with the previous one, and a YAML linter that rejects any line ending
in whitespace runs before every merge. The scripts are edited by many people, on Linux, macOS and
Windows, some pasted from indented documentation. Each edit made the generated file change in
ways nobody intended.

Finish `runBlock(...)` in `src/main/java/practice/PipelineStep.java`. `render(...)` already puts
what it returns under `run: |`.

When you're done, every script line sits six spaces in, keeping its indentation relative to the
script's other lines; the result is the same whether or not the file ends with a line break and
whichever line endings it uses; and nothing the linter rejects reaches the file. Every line of the
result, the last one included, ends with `\n`. About four lines in total.
