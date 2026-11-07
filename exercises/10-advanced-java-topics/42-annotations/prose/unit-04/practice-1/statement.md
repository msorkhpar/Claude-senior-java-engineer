The order platform's message listeners name their broker topic in an annotation,
`@Topic("orders.created")`, and the build writes the list of topics the service subscribes to,
`practice.generated.Topics`. Last week a listener marked `@Topic("Billing.Refunded")` compiled,
shipped, and failed only when the broker refused the subscription in staging.

`TopicProcessor` already writes the list. Finish `check(...)` in
`src/main/java/practice/TopicProcessor.java`, which is called for every listener with its topic.
When a topic is one the broker refuses (`wellFormed` tells you which), no service may come out of
the compile, and the compiler's output must show the developer which listener has the bad topic,
by its file and line, and what the topic is. About three lines in total.

The tests compile small listener classes with `javax.tools.JavaCompiler` and your processor, and
read what javac reports.
