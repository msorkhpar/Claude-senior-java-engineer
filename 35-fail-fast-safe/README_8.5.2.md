# 8.5.2. Applying Fail-Fast and Fail-Safe Principles in Concurrent Collections

A device gateway keeps hit counters and sessions in `ConcurrentHashMap`s, and jobs waiting for a
retry in a `ConcurrentLinkedQueue`. Some threads walk these collections while others write to
them, and no walk has ever thrown. Yet the counters it flushes add up to less than the request
log, a device that has just reconnected gets disconnected for inactivity, and the status page
sometimes fails with `Duplicate key job-1407`.

Unit 6.5.3 showed the other two kinds of iterator: a fail-fast one usually throws at the next step
after a structural change, and a `CopyOnWriteArrayList` one walks the array it started with. The
iterators here are **weakly consistent**. The `java.util.concurrent` package javadoc says they
"will never throw `ConcurrentModificationException`" and "are guaranteed to traverse elements as
they existed upon construction exactly once, and may (but are not guaranteed to) reflect any
modifications subsequent to construction". Each bug above is code that reads more into that
promise than it says. Every output below is from Java 21 on one 20-core machine. Where a count
depends on timing, we ran the program three times and give all three.

## A Concurrent Add Is Seen or Missed by Where It Lands, So Walking and Then Clearing Loses Writes

A walk over 1,000 keys pauses halfway while another thread adds one key, then carries on. We did
this 1,000 times, with a new key each time, and the same with a `ConcurrentLinkedQueue`:

```java
for (String k : map.keySet()) {
    if (++walked == 500) writer.submit(() -> map.put(added, 0)).get();  // another thread adds
    if (k.equals(added)) seen = true;
}
```

```text
ConcurrentHashMap:     add seen in 473 of 1000 walks
ConcurrentLinkedQueue: add seen in 1000 of 1000 walks
```

All three runs printed 473. The table is an array of bins, each holding the few keys that hash
there, and the iterator goes through it bin by bin. A key that lands in a bin already passed is
missed, and one that lands ahead is seen. Timing only decides where the walk is when the add
lands. A queue's iterator follows the links from head to tail, and an add goes on the tail, so it
is seen if it lands before the iterator reaches the last element.

Request threads count hits per page with `pending.merge(page, 1L, Long::sum)`, and every
millisecond a flusher writes the counts out and empties the map. Which hits can it lose?

```java
static void flushWalkThenClear() {
    for (Map.Entry<String, Long> e : pending.entrySet()) write(e.getKey(), e.getValue());
    pending.clear();
}

static void flushRemoveEach() {
    for (String page : pending.keySet()) {
        Long hits = pending.remove(page);          // takes the count and the key in one step
        if (hits != null) write(page, hits);
    }
}
```

Four request threads count hits on 5,000 pages while the flusher runs 1,000 times:

```text
walk, then clear()     hits 37,598,528, written 34,783,734, still pending 57,553, lost 2,757,241
remove(page) per key   hits 50,989,760, written 50,943,010, still pending 46,750, lost 0
```

The other two runs lost 3,833,657 and 4,177,461 hits with `clear()`, and none with `remove`.
`clear()` removes what is in the map when it runs, so it also drops pages added behind the walk
and hits merged into a page after the walk wrote it. `remove(page)` returns the count it
takes out. A hit that arrives a moment later starts a new entry, and the next flush writes it.

A queue has the same trap: `for (Integer job : queue) send(job); queue.clear();` loses whatever
arrives after the walk passes the tail, 851,334, 805,795 and 763,917 jobs of about 9 million in
three runs. `while ((job = queue.poll()) != null) send(job);` takes each job off the queue as it
sends it, and lost none.

<!-- practice 1 -->

## A Walk Hands Out Entries Read Earlier, So Act on One with remove(key, value)

Both iterators read one element ahead. A `ConcurrentHashMap` iterator finds the next node as it
returns the current one, and `next()` returns that node even if it has been removed since. Here
`first` is `ann`, the first key in iteration order, removed after the iterator is created:

```java
Iterator<Map.Entry<String, Integer>> it = m.entrySet().iterator();
m.remove(first);
Map.Entry<String, Integer> e = it.next();
```

```text
CHM first=ann next()=ann=3 containsKey=false
CLQ polled=j1 next()=j1 queue=[j2, j3]
```

With a queue, a job polled after the iterator was created still comes out of it. Both fit the
javadoc, since the element existed when the walk began. An entry from a
walk is a reading from some time during the walk, not the mapping now.

An idle reaper walks about 10,000 sessions while clients reconnect, and each reconnect puts a new
`Session(client, lastSeen)` record under the client's id. For each idle session the reaper sends a
"disconnected for inactivity" notice, here a pause of about 70 microseconds, and removes it:

```java
for (Map.Entry<String, Session> e : sessions.entrySet()) {
    if (e.getValue().lastSeen() < cutoff) {
        notice(e.getKey());
        Session gone = sessions.remove(e.getKey());
        if (gone != null) removed.add(gone);
    }
}
```

The fixed version:

```java
Session s = e.getValue();
if (s.lastSeen() < cutoff && sessions.remove(e.getKey(), s)) {   // only if still this session
    notice(e.getKey());
    removed.add(s);
}
```

```text
remove(id)           100 reaps removed 5,381 sessions, 431 of them active
remove(id, session)  100 reaps removed 4,473 sessions, 0 of them active
```

In the other two runs `remove(id)` removed 435 and 447 active sessions, and
`remove(id, session)` none. `remove(id)` removes whatever the id maps to by then, and 431 times
that was a session that had reconnected after the check. `remove(key, value)` compares, with
`equals`, and removes in one atomic step. `replace(key, old, new)` does the same for an update;
`computeIfPresent` decides inside the step. The comparison works because a reconnect puts a
session with a new `lastSeen`. A mutable session touched in place would still equal itself, and
would be reaped anyway.

<!-- practice 2 -->

## Each Element Comes at Most Once, but an Element Added Again Is a New One

`ConcurrentLinkedQueue`'s javadoc says: "Elements contained in the queue since the creation of the
iterator will be returned exactly once." An element removed and added again was not contained
the whole time. A retrier takes the failed job at the head and puts it back at the tail with its
attempt count raised, every few tens of microseconds. A status page lists the 2,000 jobs by id:

```java
Map<String, Job> byId = queue.stream().collect(Collectors.toMap(Job::id, j -> j));
```

```text
toMap(id, job):        334 of 1000 listings threw
  last: Duplicate key job-1407 (attempted merging values Job[id=job-1407, attempt=1] and Job[id=job-1407, attempt=2])
toMap(id, job, later): 1000 listings, 0 missing a job
```

The other runs threw in 553 and 563 of 1,000 listings. The walk returned `job-1407`, then met its
retried copy at the tail. With `toMap(Job::id, j -> j, (earlier, later) -> later)`, the second
line, the later copy, the latest attempt, wins. Moving a job can also hide it: with a retrier
that never pauses, 5, 11 and 5 of 1,000 listings missed a job, taken from the head before the
walk got there and put back after the walk had passed the tail.

A map does the same. In a `ConcurrentHashMap` bin that is still a linked list (fewer than 8
keys), a new key goes at the end, so a key removed and put back before the walk has read past
that bin's last node comes again. Integer keys 1, 17 and 33 share a bin in a table of 16;
removing and putting back `1` when the walk is at `17` gives `[1, 17, 33, 1]`.

## size() and a Walk Answer at Different Moments

An export sizes an array with `size()` and fills it by walking, while one thread adds and removes
devices:

```java
String[] ids = new String[devices.size()];
int i = 0;
for (String id : devices.keySet()) ids[i++] = id;
```

```text
size() then walk: 461 of 1000 threw ArrayIndexOutOfBoundsException, 505 ended in null
keySet().toArray: 0 of 1000 had a null
```

In the other runs 481 and 450 threw, and 500 and 517 ended in `null`. When the map grew after
`size()`, the walk overran the array. When it shrank, the export ended in empty slots, which the
next stage either trips over with a `NullPointerException` or counts as devices. The `ConcurrentHashMap` javadoc says `size`, `isEmpty` and
`containsValue` "are typically useful only when a map is not undergoing concurrent updates in
other threads", and adequate "for monitoring or estimation purposes, but not for program
control". `keySet().toArray(new String[0])` takes the length from its own walk. A report header
that prints `size()` above rows from a walk disagrees with them the same way.

<!-- practice 3 -->

## Summary

- **A concurrent add is seen or missed by where it lands.** A walk followed by `clear()` loses
  whatever the walk didn't see; take each element out with `remove(key)` or `poll()`.
- **A walk hands out entries read earlier.** Act on one with `remove(key, value)`, `replace` or
  `computeIfPresent`, never with a plain `remove(key)` after a check.
- **Each element comes at most once, and an element added again is a new one.** A walk can return
  the same key or id twice, or miss one that moved; merge duplicates instead of assuming none.
- **`size()` and a walk answer at different moments.** Count what the walk returned.

## Reference

**Weakly consistent iteration (Java 21; "ran" is a run on this page, the rest is javadoc):**

| Question | `ConcurrentHashMap` | `ConcurrentLinkedQueue` |
|---|---|---|
| Throws `ConcurrentModificationException` | never | never |
| An element present for the whole walk | returned exactly once | returned exactly once |
| An element added during the walk | may be returned; ran: seen if it lands in a bin not yet walked | may be returned; ran: seen if added before the iterator reaches the last element |
| An element removed after the walk read ahead to it | ran: still returned | ran: still returned |
| A key or element removed and added again | ran: can come again if put back at the end of a list bin the walk hasn't left | ran: can come twice |
| `Iterator.remove()` | supported | supported |
| `size()` while others update | an estimate; `mappingCount()` returns a `long` estimate | walks the queue, O(n), "may be inaccurate" |
| One iterator used by several threads | "designed to be used by only one thread at a time" | not stated |

**Acting on what a walk returned:**

| Instead of | Use | Why |
|---|---|---|
| walk a map, then `clear()` | `v = map.remove(k)` per key | removes only what it returns |
| check `e.getValue()`, then `remove(k)` | `remove(k, v)` | removes only if still `v` |
| check, then `put(k, v2)` | `replace(k, v, v2)` or `computeIfPresent` | updates only if still `v` |
| `new T[size()]`, then fill by walking | `toArray(new T[0])` | length comes from the walk |
| walk a queue, then `clear()` | `poll()` until `null` | each element taken once |

## Challenges

1. The hit counters move to a `ConcurrentHashMap<String, LongAdder>`, so a request never replaces
   a value. Does `remove(page)` before writing still lose nothing? Find the interleaving that
   loses a hit, then decide whether it matters for a dashboard.
2. Your status page must list each job once with its latest attempt, and show a total that
   matches its rows. Sketch it on a `ConcurrentLinkedQueue`, then work out what it would take to
   promise that the list was true at a single moment.
3. Measure how many active sessions the reaper removes as the notice takes 1, 10 and 100
   microseconds. What does that say about a reaper that makes a network call?

## Design Note: Why a Walk Doesn't Stop the Writers

A `ConcurrentHashMap` could give a walk one consistent moment by locking the whole table, or by
copying it, as `CopyOnWriteArrayList` does on every write. Either would cost the writers: a lock
makes every `merge` wait out a walk over 5,000 pages, and unit 8.5.3 measures what a copy per
write costs. The concurrent collections let walks and writes run together.

The cost moves to the caller. Code that acts on a walk acts through the map's atomic methods.
Code that needs one moment, a report that must balance or an export that must be complete, pays
for it itself: it copies under a lock the writers also take, or keeps the number in one place
that each update changes. Unit 8.5.1 applies the opposite principle, failing at once, to an
object's own inputs.

## Code Examples

- Test: [FailFastIteratorDemoTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/failfastsafe/FailFastIteratorDemoTest.java)
- Source: [FailFastIteratorDemo.java](src/main/java/com/github/msorkhpar/claudejavatutor/failfastsafe/FailFastIteratorDemo.java)
- Test: [FailSafeIteratorDemoTest.java](src/test/java/com/github/msorkhpar/claudejavatutor/failfastsafe/FailSafeIteratorDemoTest.java)
- Source: [FailSafeIteratorDemo.java](src/main/java/com/github/msorkhpar/claudejavatutor/failfastsafe/FailSafeIteratorDemo.java)
