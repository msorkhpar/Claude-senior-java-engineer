A travel agency's agents change a booked trip by editing an amendment of it: they drop a leg,
add one, move a passenger to another seat, add a bag. The amendment replaces the booking only
when the customer confirms it. Last week a customer who never confirmed anything found their
return flight gone from the booking and a bicycle on their first leg.

`Itinerary` and its `Leg` already hold a trip. Finish the two `TODO`s in
`src/main/java/practice/Itinerary.java`:

- `Leg.copy()`: a leg like this one, for an amendment;
- `amendment()`: the itinerary the agent edits.

When you're done, an amendment starts as the booked trip, the agent can make any of those changes
to it, and none of them shows up on the booking. About five lines in total.
