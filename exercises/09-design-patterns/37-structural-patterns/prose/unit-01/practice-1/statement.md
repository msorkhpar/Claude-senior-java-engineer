The page's Q2 adapts one legacy player to the `MediaPlayer` interface in two
ways:

- an **object adapter** holds a `LegacyAudioPlayer` and **delegates to the
  player it was given**, so it works with any subclass of it;
- a **class adapter** `extends LegacyAudioPlayer implements MediaPlayer` and
  **calls the method it inherits**.

Neither adds behaviour: **the call passes through unchanged**, and whatever the
legacy player throws reaches the caller.

Given: `MediaPlayer` and `LegacyAudioPlayer` (its `playMp3` refuses a blank name
with `IllegalArgumentException`). Write both adapters:

- `new AudioPlayerAdapter(player)`: **`null` is refused at once** with
  `NullPointerException`; `getPlayerType()` is `"Audio (Object Adapter)"`.
- `new AudioClassAdapter()`: `getPlayerType()` is `"Audio (Class Adapter)"`.

| adapter | call | answer |
|---|---|---|
| object, around `new LegacyAudioPlayer()` | `play("song.mp3")` | `"Playing MP3: song.mp3"` |
| class | `play("song.mp3")` | `"Playing MP3: song.mp3"` |
| object or class | `play(" song.mp3")` | `"Playing MP3:  song.mp3"` |
| object or class | `play("  ")` | `IllegalArgumentException` from the legacy player |
| object | `new AudioPlayerAdapter(null)` | `NullPointerException` |
