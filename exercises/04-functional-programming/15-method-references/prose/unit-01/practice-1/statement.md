A puzzle game shows two standings after every round: the best score, and the fewest moves anyone
needed. Last release, players complained that the board named the wrong winners: in some rounds
the "best score" was simply the first one played, and the "fewest moves" the last one recorded.
The code that picked them was rewritten in a hurry and lost.

`Leaderboard` already streams the round's numbers into `max` and `min`. Finish the two `TODO`
methods in `src/main/java/practice/Leaderboard.java`, `byScore()` and `byMoves()`, which give the
order each standing ranks its numbers in. About two lines in total.

The tests feed rounds in which the winner sits anywhere in the list, not only where a hand-typed
test list would put it.
