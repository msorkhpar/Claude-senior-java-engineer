The document service reads the text out of every uploaded scan, a few seconds of OCR each. On
a normal day three workers keep up. At the end of the month the accountants upload in bursts,
and last month the service ran out of memory with forty thousand scans waiting.

The team's decision: three workers normally, and at most six scans waiting for one. When six are
already waiting, the service may take on extra workers for the burst, five at most in total. A
scan that arrives when all five are busy and six are waiting must not be lost or refused: the
upload thread that brought it reads it itself, which slows that uploader down until the burst
passes.

Write `create()` in `src/main/java/practice/OcrPool.java`. The class already has the numbers and a
`ThreadFactory` that names the threads. About five lines in total.

The tests hand scans to your pool from a thread named `uploader` and read the pool's size and its
queue.
