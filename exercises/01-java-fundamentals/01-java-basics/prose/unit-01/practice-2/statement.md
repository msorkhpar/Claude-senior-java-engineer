Before a build reaches every server it runs as a canary, and `DeployGate` decides whether to
promote it. The gate is configured with the highest error rate it still promotes, and the
release team has two complaints. With a limit of 30 %, the gate held a canary that failed 3 of
its 10 requests. And last Friday it promoted a canary that had served no requests at all, because
its load balancer was misconfigured, and the build broke every server it reached.

`DeployGate.judge` already turns the metrics into an error rate. Finish the `TODO` in
`src/main/java/practice/DeployGate.java`: `allows(errorRate)` promotes a canary at or under the
limit, and holds everything else, including a canary the gate has no real rate for.
`DeployGate.TOLERANCE` is there for you to use.

About two lines in total.
