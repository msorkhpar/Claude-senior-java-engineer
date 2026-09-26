#!/bin/sh
#
# Restore this corpus's narration clips from its release volumes.
# ===============================================================
#
# Written by studyforge when the clips were packed. Regenerate, never edit.
#
#   sh .studyforge/narration-release/restore.sh
#
# Narration is optional: the site is complete without it, and this script is
# only for a reader who wants the voice. It downloads the release's volumes,
# checks each against the SHA256SUMS this corpus committed beside this script,
# joins them, refuses them unless their files are exactly the clips named in
# clips.sha256 beside it, extracts those into a staging directory, checks each
# clip's digest, moves each into the directory this corpus's pages play it
# from, and deletes the downloaded volumes. The pages ask their first clip
# themselves, so nothing else is written. Running it again gives the same
# tree. A site built into another directory than this corpus's
# root has its own copies: build it again after restoring.
#
# In a clone whose `origin` is the repository on GitHub this needs no setting:
# the repository is read from that remote and the tag is the one the clips were
# packed under. Every setting below overrides one of those.
#
#   NARRATION_TAG=<tag>             another release of this corpus
#   NARRATION_REPO=<owner>/<repo>   another repository
#   NARRATION_BASE_URL=<url>        a public release's download address
#   NARRATION_LOCAL_DIR=<dir>       volumes already on this disk, read in place
#   NARRATION_KEEP_DOWNLOADS=1      keep the downloaded volumes afterwards
#
# A PRIVATE repository's release assets are not served at the public download
# address; they come through the API by asset id. Export GITHUB_TOKEN (or
# GH_TOKEN), or install `gh` and run `gh auth login`. The token is read from
# the environment, passed to curl on its standard input rather than its command
# line, and never printed.
#
# The repository and the base URL default to a placeholder: this file is
# committed, and an account name does not belong in it.
#
# Needs: curl, or a logged-in gh (neither with NARRATION_LOCAL_DIR), sha256sum
# or shasum, and unzip or python3. A private release's asset list is read with
# curl and python3.

set -u

HERE=$(cd "$(dirname "$0")" && pwd) || exit 1
ROOT=$(cd "$HERE/../.." && pwd) || exit 1
WORK="$HERE/download"
VOLUME=narration.zip
SUMS=SHA256SUMS
TAG="${NARRATION_TAG:-narration-1.0.0}"
LOCAL_DIR="${NARRATION_LOCAL_DIR:-}"
KEEP="${NARRATION_KEEP_DOWNLOADS:-}"
API="${NARRATION_API_URL:-https://api.github.com}"
TOKEN="${GITHUB_TOKEN:-${GH_TOKEN:-}}"

say() { printf '%s\n' "$*"; }
die() { printf 'restore: %s\n' "$*" >&2; exit 1; }
has() { command -v "$1" >/dev/null 2>&1; }

# The clone knows which repository it came from, so it is asked rather than
# told: no account name has to live in this file for the default to work.
detect_repo() {
  url=$(git -C "$ROOT" remote get-url origin 2>/dev/null) || return 1
  url=${url%.git}
  case "$url" in
    *github.com[:/]*) url=${url#*github.com[:/]} ;;
    *) return 1 ;;
  esac
  case "$url" in
    */*/* | *[!A-Za-z0-9._/-]* | /* | */) return 1 ;;
    */*) printf '%s' "$url" ;;
    *) return 1 ;;
  esac
}

REPO="${NARRATION_REPO:-$(detect_repo || true)}"
BASE_URL="${NARRATION_BASE_URL:-https://github.com/${REPO:-OWNER/REPO}/releases/download/$TAG}"

has sha256sum || has shasum || die "neither sha256sum nor shasum is installed"
has unzip || has python3 || die "neither unzip nor python3 is installed"
[ -n "$LOCAL_DIR" ] || has curl || has gh || die "neither curl nor gh is installed"

sha_of() {
  if has sha256sum; then sha256sum "$1" | cut -d' ' -f1; else shasum -a 256 "$1" | cut -d' ' -f1; fi
}

# The token goes to curl as a header read from standard input, so it is never
# on a command line another process can list.
with_token() {
  printf 'Authorization: Bearer %s\n' "$TOKEN" | curl -fsSL -H @- "$@"
}

ASSETS=""
load_assets() {
  [ -n "$ASSETS" ] && return 0
  has curl && has python3 || die "curl and python3 are needed to read a private release"
  ASSETS=$(with_token -H "Accept: application/vnd.github+json" \
      "$API/repos/$REPO/releases/tags/$TAG" | python3 -c '
import json, sys
for asset in json.load(sys.stdin).get("assets", []):
    print(asset["name"], asset["id"])
') || die "cannot read release $TAG of $REPO; check the repository, the tag and the token"
  [ -n "$ASSETS" ] || die "release $TAG of $REPO lists no assets"
}

fetch() {                       # fetch <name>: into PARTS, unless it is there already
  name=$1
  dest="$PARTS/$name"
  [ -s "$dest" ] && return 0
  [ -n "$LOCAL_DIR" ] && return 1
  rm -f "$dest.partial"
  if [ -n "$TOKEN" ] && [ -n "$REPO" ]; then
    load_assets
    id=$(printf '%s\n' "$ASSETS" | awk -v n="$name" '$1 == n { print $2; exit }')
    [ -n "$id" ] || return 1
    # The octet-stream Accept header is what returns the file rather than its
    # description; -L follows the redirect to where the bytes are stored.
    with_token -H "Accept: application/octet-stream" -o "$dest.partial" \
      "$API/repos/$REPO/releases/assets/$id" || return 1
  elif [ -n "$REPO" ] && [ -z "${NARRATION_BASE_URL:-}" ] && has gh \
      && gh release download "$TAG" --repo "$REPO" --pattern "$name" \
           --dir "$PARTS" --clobber >/dev/null 2>&1 && [ -s "$dest" ]; then
    return 0
  else
    has curl || return 1
    curl -fsSL --retry 3 -o "$dest.partial" "$BASE_URL/$name" || return 1
  fi
  mv -f "$dest.partial" "$dest" && [ -s "$dest" ]
}

listing() {                     # listing <zip>: every member name, one per line
  if has unzip; then
    unzip -Z1 "$1"
  else
    python3 -c 'import sys, zipfile; print("\n".join(zipfile.ZipFile(sys.argv[1]).namelist()))' "$1"
  fi
}

extract() {                     # extract <zip> <dir>, stamping each file with now
  if has unzip; then
    unzip -qq -o -DD "$1" -d "$2"
  else
    python3 -m zipfile -e "$1" "$2"
  fi
}

# The release is checked against what THIS corpus committed when it was packed,
# never only against itself: a checksum file from the same release proves the
# download intact, not that the release belongs here.
KEPT_SUMS="$HERE/$SUMS"
KEPT_CLIPS="$HERE/clips.sha256"
[ -s "$KEPT_SUMS" ] && [ -s "$KEPT_CLIPS" ] \
  || die "this corpus carries no record of a packed release; nothing was fetched"
while IFS= read -r line; do
  path=${line#*  }
  case "$path" in
    "" | /* | ../* | */../* | */.. | .. | *\\*)
      die "clips.sha256 names a path outside this corpus" ;;
  esac
done < "$KEPT_CLIPS"

STAGE=""
unstage() { [ -n "$STAGE" ] && rm -rf "$STAGE"; STAGE=""; }
refuse() { unstage; die "$*"; }

PARTS="${LOCAL_DIR:-$WORK}"
[ -n "$LOCAL_DIR" ] || mkdir -p "$WORK" || die "cannot create the download directory"

parts=$(awk '{ print $2 }' "$KEPT_SUMS" | LC_ALL=C sort)
[ -n "$parts" ] || die "$SUMS names no volume"
count=0
for part in $parts; do
  case "$part" in
    "$VOLUME".[0-9][0-9][0-9]) ;;
    *) die "$SUMS names a file that is not a volume" ;;
  esac
  count=$((count + 1))
done
say "narration: $count volume(s) at $TAG"

for part in $parts; do
  if ! fetch "$part"; then
    rmdir "$WORK" 2>/dev/null
    die "could not get $part of ${REPO:-this corpus} at $TAG; for a private repository \
export GITHUB_TOKEN or run gh auth login, or set NARRATION_LOCAL_DIR to volumes on this disk"
  fi
done

# Every volume is checked BEFORE anything is joined or extracted, against the
# digests this corpus committed: a truncated download, or a release of another
# corpus or another pack, is refused here.
for part in $parts; do
  want=$(awk -v n="$part" '$2 == n { print $1; exit }' "$KEPT_SUMS")
  if [ "$(sha_of "$PARTS/$part")" != "$want" ]; then
    [ -n "$LOCAL_DIR" ] || rm -f "$WORK/$part"
    die "checksum mismatch on $part: it is not the volume this corpus packed. Nothing was \
extracted. Run again to download it afresh"
  fi
done
say "narration: checksums ok"

mkdir -p "$WORK" || die "cannot create the download directory"
joined="$WORK/$VOLUME"
: > "$joined" || die "cannot join the volumes"
for part in $parts; do
  cat "$PARTS/$part" >> "$joined" || die "cannot join the volumes"
done

# The volumes are redundant once joined. Only the download directory is ever
# emptied: a NARRATION_LOCAL_DIR is the reader's own, not a cache made here.
if [ -z "$LOCAL_DIR" ] && [ -z "$KEEP" ]; then
  for part in $parts; do rm -f "$WORK/$part"; done
fi

# Every member must be a clip this corpus committed, by exactly its path, and
# nothing else: a member the record does not name is refused before anything is
# extracted, so a restore never writes a file that is not one of its clips.
want=$(awk '{ print substr($0, 67) }' "$KEPT_CLIPS" | LC_ALL=C sort)
have=$(listing "$joined" | LC_ALL=C sort) || { rm -f "$joined"; die "the volumes are not a zip"; }
if [ "$have" != "$want" ]; then
  rm -f "$joined"
  die "the volumes hold files that are not this corpus's clips; nothing was extracted"
fi

# Extracted into a staging directory first, each file checked against its
# committed digest, and only then moved into place.
STAGE="$WORK/staging"
rm -rf "$STAGE"
mkdir -p "$STAGE" || refuse "cannot create the staging directory"
extract "$joined" "$STAGE" || { rm -f "$joined"; refuse "extracting the clips failed"; }
rm -f "$joined"
while IFS= read -r line; do
  digest=${line%%  *}
  path=${line#*  }
  staged="$STAGE/$path"
  [ -f "$staged" ] && [ ! -L "$staged" ] || refuse "the volumes lack $path; nothing was placed"
  [ "$(sha_of "$staged")" = "$digest" ] || refuse "$path is not the clip this corpus packed"
  [ ! -d "$ROOT/$path" ] && [ ! -L "$ROOT/$path" ] \
    || refuse "$path is taken by something that is not a clip; nothing was placed"
done < "$KEPT_CLIPS"
while IFS= read -r line; do
  path=${line#*  }
  mkdir -p "$(dirname "$ROOT/$path")" && mv -f "$STAGE/$path" "$ROOT/$path" \
    || refuse "placing the clips failed at $path"
done < "$KEPT_CLIPS"
unstage
say "narration: restored into this corpus's audio directories"

# rmdir, never a recursive delete: the directory goes only when it is empty.
if [ -d "$WORK" ] && ! rmdir "$WORK" 2>/dev/null; then
  say "narration: volumes kept in $WORK; remove it when you are done"
fi

# The pages ask their first clip themselves, so nothing else is written.
say "narration: the pages will play the clips from their next load"
exit 0
