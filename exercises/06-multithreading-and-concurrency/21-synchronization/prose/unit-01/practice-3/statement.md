The page's livelock: two people in a hallway keep stepping to the same side to
let each other pass. Nobody is blocked, yet nobody gets through. It needs the
two to react **in step**, and its fix is to break the symmetry, for example with
**randomized backoff**. The course's `LivelockDemo` makes the two walker threads
move in lock step with a `Phaser`, and a round limit makes the livelock end.

Write `Hallway`, with two walker threads that move in lock step, round by round:

1. Each walker decides whether it **steps forward** this round, and makes that
   decision visible to the other.
2. When both have decided, each looks at the other. A walker that stepped
   forward while the other did not **gets through**: it is done, and from then
   on it no longer takes part in the lock step. If both stepped forward, that is
   a **conflict**, and both politely step back.
3. A walker that has used `maxRounds` rounds without getting through stops.

- `Outcome runPolite(int maxRounds)`: a polite walker steps forward every round.
- `Outcome runWithBackoff(int maxRounds, long seed)`: a walker steps forward in
  its first round, and in each round after a conflict it steps forward only when
  its own `Random`'s `nextBoolean()` is `true`. Walker 1 uses
  `new Random(seed)`, walker 2 `new Random(seed + 1)`.

`Outcome(walker1Done, walker2Done, walker1Rounds, walker2Rounds)` is given; a
walker's rounds are the number of rounds it took part in. Both methods return
only when both walker threads have finished.

| call | answer |
|---|---|
| `runPolite(25)` | neither is done; both used 25 rounds |
| `runWithBackoff(1, 7)` | neither is done; both used 1 round |
| `runWithBackoff(200, seed)` for any seed | both are done, in different rounds |
