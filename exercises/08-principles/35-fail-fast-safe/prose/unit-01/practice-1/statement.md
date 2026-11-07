The warehouse takes shipments from a carrier's feed in the afternoon, and the dispatch run prints
the pallet sheets at night. Last week the dispatch run stopped on a shipment with no destination,
and nobody could tell which feed message it came from. The week before, a shipment of zero parcels
printed a sheet with nothing on it, and a pallet left unlabelled.

`Shipment` already stores what the feed sends and prints its routing code. Finish the two `TODO`s
in `src/main/java/practice/Shipment.java`:

- `requireFields(...)`: a shipment needs a tracking id that isn't blank, a destination, and
  between 1 and 20 parcels;
- `labelsToKeep(...)`: the labels the shipment stores.

When you're done, a shipment the dispatch run couldn't handle is refused when the feed creates it:
a missing value with a `NullPointerException`, a present but unusable one with an
`IllegalArgumentException`. Every label is present, and the feed can't change an accepted
shipment's labels afterwards. About six lines in total.
