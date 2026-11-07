A service records metric lines into a `MetricsBuffer`. Every ten seconds a shipper thread calls
`drain()` and sends the list it gets back to the metrics collector. Since the last release the
collector's graphs for this service are flat, though the shipper logs a shipment every ten
seconds and the buffer never grows.

Finish `drain()` in `src/main/java/practice/MetricsBuffer.java`: it hands over the lines
recorded since the last drain, and leaves the buffer collecting a new batch. About three lines.
