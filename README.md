# Claude Senior Java Engineer Interview Preparation

This repository is the whole course, ready to study on your own machine: its
lessons as a study site, its practices with a runner that grades them, and an
editor to write your answers in. Everything runs in Docker, on this machine only.

## What you need

Docker: Docker Desktop on Windows or macOS, or Docker Engine with the compose
plugin on Linux. Nothing else: no Java, no Python, no other download.

## Start it

1. Install Docker, and start it.
2. Clone this repository and open a terminal in its directory.
3. Copy `course.env` to `.env`, and set `STUDYFORGE_NAMESPACE` in it to the Docker Hub
   account you were given: the images are published under that account.

   ```
   cp course.env .env
   ```

   On Windows, use `copy course.env .env`.
4. Start the course from the published images:

   ```
   docker compose -f compose.pull.yaml up -d
   ```

   The first start downloads the images, which takes a while.
   Until `STUDYFORGE_NAMESPACE` is set, compose stops and says so.
5. Open http://127.0.0.1:8772/ in your browser.

The images are built for amd64 (Intel and AMD). On an Apple Silicon Mac they run
under emulation, which is slower.

To build every image from this checkout instead, which needs no account (the
first build takes a while, and downloads only pinned base images and the
course's pinned dependencies):

```
docker compose up -d --build
```

To see what is running: `docker compose -f compose.pull.yaml ps`. Use the same
`-f compose.pull.yaml` with every compose command for the pulled course.

## The ports

| What | Where |
|---|---|
| The study site | http://127.0.0.1:8772/ |
| The editor, which the site opens inside each practice | http://127.0.0.1:8444/ |

Both listen on this machine only. To use other ports, copy `course.env` to
`.env` and change `COURSE_SITE_PORT` and `COURSE_EDITOR_PORT`.

## Study and practise

Open a lesson from the site's contents and read it. A lesson with practices
lists them after the text: open one, and the editor shows its file beside the
task. **Run** runs your code; **Submit** runs the practice's tests in the
runner, which has no network, and marks the practice passed when they pass.

Your answers, your progress and the editor's settings live in Docker volumes,
so they survive a restart. `down` stops the course and keeps them; `down -v`
deletes them too.

## Narration (optional)

The site is complete without narration: a lesson with no recording shows no
player. To hear the lessons read aloud, set
`COURSE_NARRATION=with-narration` in `.env` (copy it from `course.env`), then:

- **Pulled images**: `docker compose -f compose.pull.yaml up -d` pulls the voiced site. Leave the setting as it is for the site without narration.
- **Building it yourself**: download the recordings from the course's release
  first, then build:

```
sh .studyforge/narration-release/restore.sh
docker compose up -d --build
```

  On Windows, run `pwsh .studyforge/narration-release/restore.ps1` instead of the first line.
  The script checks every download and every recording against the checksums
  in this repository before it places anything, and deletes the downloads
  afterwards. Set `NARRATION_LOCAL_DIR` to a directory holding the release's
  volumes to read them from disk instead of downloading them.

## The exercises

`exercises/` holds the course's authored exercises: each one's statement,
starter, tests and reference solution. `practice/` holds your working copy of
each: the files you edit and submit live there, and `exercises/` is only read.

## Stop it

```
docker compose -f compose.pull.yaml down
```

(`docker compose down` stops a course started with the build file.)

## Licence

The course's licence is in `LICENSE`.

## How this course was built

How it was built lives on the `studyforge/build` branch; you do not need it.
