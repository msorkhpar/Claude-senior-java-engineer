The outdoor-gear chain's nightly report says which products sold in which region. Several report
threads share one `RegionReport`, and to save allocations each thread renders into a buffer it
keeps between reports. The test data had a single region and the report looked right; the first
real night, regional managers complained that their section listed products they don't stock, and
one report came out with half of another report's lines in it.

Finish `skusByRegion` and `newScratch` in `src/main/java/practice/RegionReport.java`. Each region
lists only its own SKUs, sorted and without repeats, and each rendering thread has a buffer that
no other thread touches and that it reuses from one report to the next. About three lines in
total.
