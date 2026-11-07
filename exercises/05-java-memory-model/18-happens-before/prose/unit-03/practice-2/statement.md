The paging service keeps each team's current on-call `Rota` in a plain field, and the alert threads
read it with no lock, many times a second. The admin UI builds a rota from the lists an admin is
editing and, when the admin presses Publish, stores it in that field. Last Tuesday night the payments
team's backup was paged instead of its primary: an admin had published the week's rota, then went
on reordering Tuesday in the editor for next week's draft, without publishing again.

Finish the `TODO` in `src/main/java/practice/OnCall.java`, the record's compact constructor. When
you're done, a rota pages the people it was built with, in the order it was built with, for as long
as it lives: whatever the editor does to its lists afterwards, and whatever a caller tries with the
lists and the map the rota hands out. About four lines in total.

The tests build rotas from fresh mutable lists and maps, the way the editor does, and change them
afterwards.
