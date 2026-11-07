The shipment desk pastes tracking references from carrier e-mails into a form. A reference is two
capital letters, nine digits and two capital letters, and the pattern is already in
`TRACKING_REF`. Anything else goes back to the sender for correction, because the carrier's
tracking API rejects it a day later. The desk also lists shipments by carrier; a shipment with a
`null` carrier hasn't been handed to one yet, and the desk asks for those by passing `null`.

Write `validRef()` and `byCarrier(...)` in `src/main/java/practice/Shipments.java`. A pasted text
is valid only if it is a reference and nothing else. The carrier filter must work for every
carrier, `null` included, on a list where some shipments have no carrier. About two lines in
total.
