A sensor gateway stores temperature batches and keeps a `HashSet<Batch>` of what it has stored, so
that a batch a sensor sends twice (it resends whenever an acknowledgement is lost) is stored once.
In production the set never recognises a resent batch, and the gateway's storage has doubled. A
second report says that some stored batches later show samples from a different read.

`Batch` is a record with a `String device` and an `int[] samples` component. Finish the four
`TODO`s in `src/main/java/practice/Batch.java`:

- `equals` and `hashCode`: a resent batch is the same device with the same samples in the same
  order;
- `keep(...)`: what the batch stores, given the array the sensor driver passes in (the driver reads
  every batch into one buffer and reuses it);
- `samples()`: what a reader of a stored batch gets back.

When you're done, the gateway's set drops every resent batch, and no code outside `Batch` can
change a batch once it is built. About eight lines in total.
