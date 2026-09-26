The page's virtual proxy stands in for an expensive `RealImageService` and
creates it only when it is really needed. The client sees the same
`ImageService` interface either way. Its pitfalls section adds a warning: a
lazy `if (service == null)` check is a race when threads share the proxy.

Given: `ImageService` (`display()`, `getFilename()`, `getSize()`). Write
`VirtualImageProxy implements ImageService`. The constructor takes the filename
and a `loader` (`Function<String, ImageService>`) that builds the real image,
so the tests can count loads.

- The constructor loads nothing, but **a blank filename is refused in the
  constructor** with `IllegalArgumentException`.
- **`getFilename` does not load the image**: the proxy already knows it.
- `display()` and `getSize()` load the image on first use and delegate to it.
  **The image is loaded once and reused**, and even when **racing first calls**
  come from two threads, **they load the image once**.
- `isLoaded()` says whether the real image exists yet.

| calls | loads | answer |
|---|---|---|
| `new VirtualImageProxy("photo.png", loader)` | 0 | a proxy |
| then `getFilename()` | 0 | `"photo.png"` |
| then `display()` | 1 | `"Displaying image: photo.png"` |
| then `getSize()`, `display()` | still 1 | the real image's answers |
| `new VirtualImageProxy("  ", loader)` | 0 | `IllegalArgumentException` |
