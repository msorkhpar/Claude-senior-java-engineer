# check=skip=InvalidDefaultArgInFrom
# ⭐ That directive is deliberate: the linter warns that an ARG used in FROM has
# no default, and having NO default is the point (see below).
#
# The EDITOR image: code-server, with the runner's pinned toolchains and
# pinned extensions, and a shell that finds every toolchain.
# ⭐ For a DECLARED SET of runtimes: every tree, PATH entry, JAVA_HOME,
# extension and seed block below belongs to a declared runtime, and nothing to
# an undeclared one.
#
# ⛔ Build it ONLY through `docker/editor/build.py`, from the component root:
#     python3 docker/editor/build.py --runtimes java,maven
# It builds the runner for the same set first, then this file. Every ARG below
# is passed from `pins.json` / `editor-pins.json` and carries NO default, so a
# build that bypasses them fails on an empty argument instead of choosing.
#
# ⛔ This file chooses NO runtime version. Every runtime is COPIED out of the
# runner image (`docker/minimal/`) built for the same set, so `pins.json` stays
# the one place a runtime version is chosen.
# ⛔ No stage here is `FROM scratch`: a bare scratch stage was served
# from another stage's cache and put every runtime in a `python` runner.
# ⛔ No Docker socket, no `docker` CLI. The reader's code runs in the RUNNER,
# never here: the extraction source's `exec` mode is dropped.
#
# ⚠️ OVER THE 400-LINE BOUND FOR A SOURCE FILE, AND JUSTIFIED HERE. A Dockerfile has no
# import, so it cannot be split into modules; each step keeps its reason beside
# it, and the tests read the steps by their text.

ARG RUNNER_IMAGE
ARG EDITOR_BASE
ARG FETCH_IMAGE
ARG WITH_JAVA

# ------------------------------------------------------------------- toolchain
FROM ${RUNNER_IMAGE} AS toolchain

# ----------------------------------------------------------------------- fetch
# Every extension and the TypeScript tarball, each checked against the sha256
# RECORDED in editor-pins.json. No checksum is fetched: a host that serves a
# different file fails the build here, naming it.
FROM ${FETCH_IMAGE} AS fetch
ARG FETCH
COPY docker/editor/fetch.py /fetch.py
RUN printf '%s\n' "${FETCH}" > /tmp/fetch.txt \
 && python3 /fetch.py /tmp/fetch.txt /fetch \
 && rm /tmp/fetch.txt

# -------------------------------------------------------------------- lockdown
# The practice-focus extension, this repository's own artifact and no
# consumer's: plain CommonJS against the `vscode` module the workbench
# provides, no build step and no dependencies. It is PACKAGED into a .vsix here
# and INSTALLED below, never copied into the extensions directory -- the
# workbench reads `extensions.json` there and never scans, so a copied folder
# is present, correct and silently never loaded. Measured.
# ⭐ The packer is the standard library's `zipfile`, never a marketplace tool,
# and it refuses a target name its own manifest does not give: the id the build
# expects and the id inside the file can never drift apart.
# ⛔ This stage holds `lockdown/` ALONE, so nothing in it may import from
# `docker/`; the pinned Python image is the only build tool it gets.
FROM ${FETCH_IMAGE} AS lockdown
ARG LOCKDOWN_VSIX
COPY lockdown /lockdown
RUN --network=none python3 /lockdown/lockdown.py /lockdown "/packed/${LOCKDOWN_VSIX}"

# ------------------------------------------------------------------------ face
# The study page's code face, JetBrains Mono (SIL OFL 1.1), from its release
# archive by the sha256 editor-pins.json records; every file taken out of it
# is checked against its own recorded sha256, which is the page's vendored
# copy's, so the page and the editor draw code with the same bytes.
FROM ${FETCH_IMAGE} AS face
ARG FACE_URL
ARG FACE_SHA256
ARG FACE_FILES
ADD --checksum=sha256:${FACE_SHA256} ${FACE_URL} /face.zip
COPY docker/editor/face.py /face.py
RUN --network=none printf '%s\n' "${FACE_FILES}" > /tmp/face.txt \
 && python3 /face.py /face.zip /tmp/face.txt /faces

# ------------------------------------------------------------------ JAVA_HOME
# ⛔ A set without java carries NO JAVA_HOME, not an empty one and not one that
# points at nothing. ENV cannot be conditional, so the editor stage
# starts from one of two stages on the same base.
FROM ${EDITOR_BASE} AS home-java-yes
ARG JAVA_HOME_DIR
ENV JAVA_HOME=${JAVA_HOME_DIR}

FROM ${EDITOR_BASE} AS home-java-no

# ---------------------------------------------------------------------- editor
FROM home-java-${WITH_JAVA} AS editor
USER root

# The runtimes, exactly as the runner holds them: /opt is the declared set's
# trees and nothing else, and /usr/local carries the pinned Python with
# its hash-pinned pytest when python is declared. The base's own /usr/local
# holds only `fixuid`, which the copy leaves in place.
COPY --from=toolchain /opt/ /opt/
COPY --from=toolchain /usr/local/ /usr/local/

# ⚠️ The runner's Python links libreadline.so.8 from lib-dynload, and this base
# does not carry it (measured): `import readline` fails without it.
# From snapshot.debian.org at a pinned timestamp and exact versions; only the
# snapshot is consulted, so nothing arrives from a moving archive.
ARG WITH_READLINE
ARG READLINE_SNAPSHOT
ARG READLINE_PACKAGES
RUN if [ "${WITH_READLINE}" = yes ]; then \
      mkdir -p /tmp/apt-parts \
   && echo "deb [check-valid-until=no] http://snapshot.debian.org/archive/debian/${READLINE_SNAPSHOT} trixie main" \
          > /tmp/apt-snapshot.list \
   && set -- -o Dir::Etc::SourceList=/tmp/apt-snapshot.list -o Dir::Etc::SourceParts=/tmp/apt-parts \
          -o "Acquire::http::User-Agent=Example/0.1 (+https://example.invalid)" \
   && export DEBIAN_FRONTEND=noninteractive \
   && apt-get "$@" update \
   && apt-get "$@" install -y --no-install-recommends ${READLINE_PACKAGES} \
   && rm -rf /var/lib/apt/lists/* /tmp/apt-parts /tmp/apt-snapshot.list; \
    fi \
 && ldconfig

# ⭐ ENV reaches `docker exec` and every non-login shell. Its /opt entries are
# the declared set's only, then the base's own PATH.
ARG EDITOR_PATH
ENV PATH=${EDITOR_PATH}

# TypeScript into the runner's Node prefix, so `tsc` sits beside `node`. From the
# pinned tarball, with no network: nothing is resolved from a registry.
ARG WITH_TYPESCRIPT
RUN --network=none --mount=type=bind,from=fetch,source=/fetch,target=/tmp/fetch \
    if [ "${WITH_TYPESCRIPT}" = yes ]; then \
      HOME=/tmp/npm-home npm install -g --offline --no-audit --no-fund /tmp/fetch/typescript.tgz \
   && rm -rf /tmp/npm-home; \
    fi

# Extensions are installed at BUILD time into a directory IN the image, never
# onto a volume: an extension on a volume outlives the image, and after an
# upgrade the reader runs old extensions against a new toolchain.
# ⭐ With no network, from the pinned files only: nothing is quietly fetched
# from a marketplace. ⛔ Then the installed set must EQUAL the pins and every
# declared dependency must be installed, or the build fails naming the id —
# a marketplace that stops serving an id otherwise yields an image with no
# language support that still builds and starts.
# ⭐ The lockdown's .vsix is installed here too, for EVERY declared set: it
# serves no runtime, so it is no consumer's selection, and its id is in
# EXPECTED_EXTENSIONS unconditionally.
# ⛔ THE CHECK BELOW PROVES INSTALLATION AND NOTHING MORE, AND SAYING SO IS
# THE LESSON OF AN EARLIER DEFECT: it read the INSTALLED list, passed, and the image it passed had a
# lockdown the extension host never activated. ⭐ The RUNNING fact is proved
# outside this file, after the build, by `docker/editor/activation.py`, and
# the image is tagged only once that passes.
ARG EXPECTED_EXTENSIONS
COPY docker/editor/verify_extensions.js /tmp/verify_extensions.js
RUN --network=none --mount=type=bind,from=fetch,source=/fetch,target=/tmp/fetch \
    --mount=type=bind,from=lockdown,source=/packed,target=/tmp/lockdown \
    set -eu; \
    mkdir -p /opt/code-server/extensions; \
    for vsix in /tmp/fetch/*.vsix /tmp/lockdown/*.vsix; do \
      [ -e "$vsix" ] || continue; \
      code-server --extensions-dir /opt/code-server/extensions --install-extension "$vsix" >/dev/null 2>&1 \
        || { echo "installing $(basename "$vsix") failed" >&2; exit 1; }; \
    done; \
    code-server --extensions-dir /opt/code-server/extensions --list-extensions --show-versions 2>/dev/null \
      | /usr/lib/code-server/lib/node /tmp/verify_extensions.js /opt/code-server/extensions ${EXPECTED_EXTENSIONS}; \
    rm -rf /tmp/verify_extensions.js /root/.local/share/code-server /root/.config/code-server; \
    chown -R 1000:1000 /opt/code-server/extensions; \
    chmod -R a+rX /opt/code-server/extensions

# ⛔ THE BUNDLED CHAT IS DELETED FROM THE PRODUCT, not hidden behind a setting.
# ⚠️ It is a BUILT-IN of the code-server build -- it appears in no
# `--list-extensions` output and in no EXPECTED_EXTENSIONS, which is why three
# rounds of hiding panels never removed it: the reader could still reach Open
# Chat, Open Inline Chat and Open Quick Chat from the title bar, and the
# secondary side bar opened itself on it despite
# `workbench.secondarySideBar.defaultVisibility: hidden`.
# ⭐ A SETTING WOULD BE THE WRONG INSTRUMENT HERE (the command-surface lesson): a hidden
# entry point is still a capability, and a practice frame that can reach a
# model is grading theatre: a pass it could buy says nothing. Deleting the directory removes the capability
# rather than the button.
# ⭐ The `[ -d ]` guard is deliberate: a future code-server that no longer
# bundles it must not fail this build, and the ASSERTION that it is gone lives
# in `tests/test_editor_image.py` against the built image, where a silent
# rename would be caught. ⚠️ This removes the chat EXTENSION only; the
# workbench's own chat is the step after the agent host's.
RUN set -eu; \
    built_in=/usr/lib/code-server/lib/vscode/extensions; \
    for gone in copilot; do \
      [ -d "$built_in/$gone" ] && rm -rf "$built_in/$gone" || true; \
    done; \
    ! ls -d "$built_in"/*copilot* >/dev/null 2>&1

# ⛔ THE BUNDLED COPILOT CLI IS DELETED, AND THE AGENT HOST THAT RAN IT IS OFF.
# ⚠️ The chat deletion above left the SERVER half: the first workbench to
# connect made the server fork `bootstrap-fork --type=agentHost`, which ran
# `node_modules/@github/copilot-linux-x64/index.js --headless` (136 MB, with
# api.github.com and api.githubcopilot.com in it) on a container with egress.
# ⭐ THREE EDITS, each measured on 4.137.0 as needed on its own:
#  1. The modules go: the CLI, its SDK, and `@vscode/copilot-api`, the Copilot
#     API client that only the agent host imports. ALONE this is not enough:
#     the fork still starts, throws ERR_MODULE_NOT_FOUND for
#     `@github/copilot-sdk`, and stays resident.
#  2. The server forks on demand in its default branch, and no setting reaches
#     that branch; a flag would not either, because a consumer's compose
#     `command:` replaces this CMD. So the branch before it is taken instead:
#     upstream's own "no upstream agent host" path, a channel that refuses
#     every connect. Nothing is forked, whatever a client asks for.
#  3. The workbench's enablement (`remoteAuthority && !chat.disableAIFeatures`)
#     is made false in both bundles. Without it the workbench retries the
#     refused connect with a backoff, and warns in the console each time.
# ⛔ Each patch must find EXACTLY ONE anchor and leave one marker, or the build
# fails. `tests/test_editor_agent_host.py` reads the EFFECT with `ps` in a real
# session, and plants the two patches back out.
RUN set -eu; \
    vscode=/usr/lib/code-server/lib/vscode; \
    for gone in @github/copilot @github/copilot-linux-x64 @github/copilot-linux-arm64 @github/copilot-sdk \
                @vscode/copilot-api; do \
      [ -d "$vscode/node_modules/$gone" ] && rm -rf "$vscode/node_modules/$gone" || true; \
    done; \
    rmdir "$vscode/node_modules/@github" 2>/dev/null || true; \
    left="$(find "$vscode/node_modules" -maxdepth 2 -iname '*copilot*')"; \
    [ -z "$left" ] || { echo "the Copilot CLI is still in the product: $left" >&2; exit 1; }; \
    server="$vscode/out/server-main.js"; \
    found="$(grep -o 'else if([a-zA-Z_$]*\["agent-host-bridge-port"\]||' "$server" | wc -l)"; \
    [ "$found" = 1 ] \
      || { echo "$server holds $found agent host bridge branches; this step patches exactly one" >&2; exit 1; }; \
    sed -i 's/else if(\([a-zA-Z_$]*\["agent-host-bridge-port"\]||\)/else if(!0||\1/' "$server"; \
    [ "$(grep -o 'else if(!0||[a-zA-Z_$]*\["agent-host-bridge-port"\]||' "$server" | wc -l)" = 1 ] \
      || { echo "$server: the agent host was not switched off" >&2; exit 1; }; \
    for bundle in "$vscode/out/vs/code/browser/workbench/workbench.js" \
                  "$vscode/out/vs/workbench/workbench.web.main.internal.js"; do \
      found="$(grep -o 'super(!![a-zA-Z_$]*\.remoteAuthority,' "$bundle" | wc -l)"; \
      [ "$found" = 1 ] \
        || { echo "$bundle holds $found agent host enablements; this step patches exactly one" >&2; exit 1; }; \
      sed -i 's/super(!!\([a-zA-Z_$]*\)\.remoteAuthority,/super(!1\&\&!!\1.remoteAuthority,/' "$bundle"; \
      [ "$(grep -o 'super(!1&&!![a-zA-Z_$]*\.remoteAuthority,' "$bundle" | wc -l)" = 1 ] \
        || { echo "$bundle: the agent host enablement was not switched off" >&2; exit 1; }; \
    done

# ⛔ THE WORKBENCH'S OWN CHAT IS TAKEN OUT OF THE PRODUCT, WHATEVER A VOLUME HOLDS.
# ⚠️ Both deletions above left it: Code 1.137 carries a chat view of its own
# ("Build with Agent", a chat input), a title-bar chat button, a Copilot status
# item and about 470 AI commands, none of them an extension. Measured on
# 4.137.0: a reader reached the chat view in a practice frame.
# ⭐ `no_ai.js` makes four edits in both bundles: the workbench's AI switch
# DEFAULTS on, the chat entitlement is always hidden (so a settings.json that
# turns the switch back off still shows no chat), and the command registry and
# the palette refuse every AI command id. ⛔ A setting in the seed would miss
# every existing volume, whose settings.json the entrypoint never touches.
# Each edit must find exactly one anchor, or the build fails naming it.
# `tests/test_editor_no_ai.py` reads the EFFECT in a real browser, fresh and on
# an existing volume, and plants the edits back out.
RUN --network=none --mount=type=bind,source=docker/editor/no_ai.js,target=/tmp/no_ai.js \
    /usr/lib/code-server/lib/node /tmp/no_ai.js \
      /usr/lib/code-server/lib/vscode/out/vs/code/browser/workbench/workbench.js \
      /usr/lib/code-server/lib/vscode/out/vs/workbench/workbench.web.main.internal.js

# ⛔ THE PRIMARY SIDE BAR STARTS CLOSED, before the workbench's first paint.
# ⚠️ Measured on code-server 4.137.0 in a real browser: every practice frame
# painted the Explorer from the workbench's first frame for up to seven seconds,
# until the lockdown's `closeSidebar` passes won -- and each close then moved
# focus to the editor, which scrolled the reader's page to the frame.
# ⭐ WHY THE BUNDLE AND NOT A SETTING: no setting names the primary side bar's
# visibility. It is workbench layout STATE, kept per workspace in the BROWSER's
# own storage, so neither the server nor this image can seed it; with nothing
# stored the workbench falls back to `SIDEBAR_HIDDEN.defaultValue`, which is
# "hidden only for an empty window". This makes it hidden for every window,
# which is what the lockdown enforces anyway -- now from the first frame
# instead of after it.
# ⛔ Exactly ONE default per bundle before, and the hidden one after, or the
# build fails: a code-server that renamed the key must not ship a workbench that
# silently shows the Explorer again. `tests/test_editor_layout.py` reads the
# EFFECT in a real browser, and plants the upstream default back.
RUN set -eu; \
    for bundle in /usr/lib/code-server/lib/vscode/out/vs/code/browser/workbench/workbench.js \
                  /usr/lib/code-server/lib/vscode/out/vs/workbench/workbench.web.main.internal.js; do \
      found="$(grep -o 'SIDEBAR_HIDDEN\.defaultValue=[^,]*,' "$bundle" | wc -l)"; \
      [ "$found" = 1 ] \
        || { echo "$bundle holds $found side bar defaults; this step patches exactly one" >&2; exit 1; }; \
      sed -i 's/SIDEBAR_HIDDEN\.defaultValue=[^,]*,/SIDEBAR_HIDDEN.defaultValue=!0,/' "$bundle"; \
      grep -q 'SIDEBAR_HIDDEN\.defaultValue=!0,' "$bundle" \
        || { echo "$bundle: the side bar default was not replaced" >&2; exit 1; }; \
    done

# ⛔ THE SECONDARY SIDE BAR NEVER OPENS, NOT EVEN WHILE THE WORKBENCH STARTS.
# ⚠️ Measured on code-server 4.137.0 in a real browser, with every chat step
# above in place: a Java practice frame painted the secondary side bar, headed
# "Chat" (its active container `workbench.panel.chat`), for about 100 frames
# after the workbench's first paint -- on a first start and again on a reload
# -- and then it went away. The chat view container is still REGISTERED in
# the product (its view is hidden, not unregistered), so the workbench never
# learns the bar is empty, and upstream's default for a folder window,
# `workbench.secondarySideBar.defaultVisibility: visibleInWorkspace`, opens it.
# ⭐ WHY NOT A SETTING: the layout state that says whether the bar is open is
# kept in the BROWSER's storage per workspace, and a stored "open" overrides
# every default; a seeded setting would also miss every existing volume. So the
# layout's own override runs first, on every start, before the first layout:
# `applyOverrides` begins by hiding the bar, as upstream does for its agent
# sessions welcome page. Nothing in a practice frame belongs in that bar.
# ⛔ Exactly one `applyOverrides` per bundle before and one override after, or
# the build fails. `tests/test_editor_layout.py` and `tests/test_editor_no_ai.py`
# read every frame from before the workbench's first script, first start and
# reload, and plant this edit back out.
RUN set -eu; \
    for bundle in /usr/lib/code-server/lib/vscode/out/vs/code/browser/workbench/workbench.js \
                  /usr/lib/code-server/lib/vscode/out/vs/workbench/workbench.web.main.internal.js; do \
      found="$(grep -o 'applyOverrides([a-zA-Z_$]*){if(this\.isNew\[1\])' "$bundle" | wc -l)"; \
      [ "$found" = 1 ] \
        || { echo "$bundle holds $found layout overrides; this step patches exactly one" >&2; exit 1; }; \
      [ "$(grep -o 'applyAuxiliaryBarHiddenOverride([a-zA-Z_$]*){' "$bundle" | wc -l)" = 1 ] \
        || { echo "$bundle: the secondary side bar's hidden override is not there to call" >&2; exit 1; }; \
      sed -i 's/applyOverrides(\([a-zA-Z_$]*\)){if(this\.isNew\[1\])/applyOverrides(\1){this.applyAuxiliaryBarHiddenOverride(!0);if(this.isNew[1])/' "$bundle"; \
      [ "$(grep -o 'applyOverrides([a-zA-Z_$]*){this\.applyAuxiliaryBarHiddenOverride(!0);' "$bundle" | wc -l)" = 1 ] \
        || { echo "$bundle: the secondary side bar was not closed at start" >&2; exit 1; }; \
    done

# ⭐ THE EDITOR DRAWS CODE IN THE PAGE'S FACE. The workbench runs in the
# READER's browser, so the face is SERVED beside the workbench's stylesheet
# (its content policy allows fonts from its own origin only) and declared at
# the end of that stylesheet; `lockdown/package.json` makes it the editor's
# default family. ⛔ Exactly one declaration after, or the build fails.
# `tests/test_editor_face.py` reads the face the browser actually drew.
RUN --network=none --mount=type=bind,from=face,source=/faces,target=/tmp/faces \
    set -eu; \
    dir=/usr/lib/code-server/lib/vscode/out/vs/code/browser/workbench; \
    cp -R /tmp/faces "$dir/faces"; \
    cat "$dir/faces/faces.css" >> "$dir/workbench.css"; \
    [ "$(grep -c -F "/* studyforge: the page's code face */" "$dir/workbench.css")" = 1 ] \
      || { echo "$dir/workbench.css: the code face is not declared exactly once" >&2; exit 1; }; \
    chmod -R a+rX "$dir/faces"

# ⛔ THE STATIC PATH CHANGES WHENEVER THE PRODUCT'S FILES DO, AND ONLY THEN.
# ⚠️ The workbench is served under `/stable-<product.json commit>/static/`
# with `Cache-Control: public, max-age=31536000`, and that commit is
# code-server's, the same for every image built on one release. Measured: a
# browser that had loaded one image kept running its cached `workbench.js`
# (transferSize 0) against the next, so every edit above reached a reader only
# when their cache expired. ⛔ A cache header would not help: it cannot evict
# what a browser already holds.
# ⭐ So the commit becomes a digest of every file under the product's root,
# product.json included as it stood: identical product files give the same
# path (an unchanged rebuild downloads nothing), any edit a new one.
# ⚠️ The commit is BAKED into every bundle as well as product.json, and the
# server refuses a workbench whose commit differs ("Client refused: version
# mismatch", measured when only product.json was rewritten), so it is
# rewritten in every file under the product that carries it, and none is left.
# ⛔ This must stay the LAST step that writes under /usr/lib/code-server.
# `tests/test_editor_static_path.py` reads it with a real browser, A then B.
RUN --network=none set -eu; \
    vscode=/usr/lib/code-server/lib/vscode; product="$vscode/product.json"; \
    was="$(grep -o '"commit": *"[0-9a-f]*"' "$product" | grep -o '[0-9a-f]\{40\}' || true)"; \
    [ "$(printf '%s' "$was" | grep -c .)" = 1 ] \
      || { echo "$product names $(printf '%s' "$was" | grep -c .) commits; this step rewrites exactly one" >&2; exit 1; }; \
    now="$(cd "$vscode" && find . -type f -print0 | LC_ALL=C sort -z | xargs -0 sha256sum | sha256sum | cut -c1-40)"; \
    baked="$(grep -rlF "$was" "$vscode" | wc -l)"; \
    [ "$baked" -ge 2 ] || { echo "$vscode carries the commit in $baked files; the product and its bundles expected" >&2; exit 1; }; \
    grep -rlF "$was" "$vscode" | xargs sed -i "s/$was/$now/g"; \
    left="$(grep -rlF "$was" "$vscode" || true)"; \
    [ -z "$left" ] || { echo "the old commit is still in: $left" >&2; exit 1; }; \
    [ "$(grep -c "\"commit\": \"$now\"" "$product")" = 1 ] \
      || { echo "$product: the static path was not rewritten" >&2; exit 1; }; \
    echo "static path: stable-$now, rewritten in $baked files"

# ⭐ A CLOSED PRACTICE SESSION IS RELEASED IN MINUTES, NOT HOURS. A closed tab
# leaves its extension host and language server running until the server's
# reconnection grace ends, and upstream's is three hours: measured, every
# closed session held about 1.1 GiB until then. ⚠️ It stays long enough for a
# hidden tab, whose timers the browser throttles to once a minute, to
# reconnect. An ENV and not a flag, because a consumer's `command:` replaces
# CMD. `tests/test_editor_idle.py` reads both halves in a real session.
ENV CODE_SERVER_RECONNECTION_GRACE_TIME=180

# ⛔ NO EXTENSION GALLERY. With none configured, code-server points the
# workbench at open-vsx.org, and the reader's browser queried it from every
# practice frame (measured). Every extension this image carries is installed
# from a pinned file at build time, so the gallery serves nothing here; an
# empty gallery switches the marketplace, its update checks and its
# recommendations off. The flags the server needs are the entrypoint's.
ENV EXTENSIONS_GALLERY={}
# The XDG default, stated, so Buildship keeps its Gradle version list where the
# entrypoint seeds it (see the entrypoint) rather than in each workspace. It is
# the container's own layer, not a volume, and owned by the runtime uid.
ENV XDG_CACHE_HOME=/home/coder/.cache
RUN mkdir -p /home/coder/.cache && chown 1000:1000 /home/coder/.cache

# ⭐ A LOGIN shell rebuilds PATH from /etc/profile, which assigns it outright
# (measured on this base) and silently drops /opt: a tool works under
# `docker exec` and is "command not found" in the integrated terminal. So the
# same /opt entries (and JAVA_HOME, when java is declared) are set again from
# profile.d, from the plan.
ARG PROFILE_D
RUN printf '%s\n' "${PROFILE_D}" \
      > /etc/profile.d/10-editor-toolchain.sh

# The settings seed. Each `// @runtime <name>` block of an undeclared runtime is
# dropped, so no setting points at a runtime the image does not hold. Its Java
# runtime name is derived from pins.json at build time, never written by hand.
ARG JAVA_RUNTIME
ARG SEED_DROP
COPY docker/editor/entrypoint.sh /opt/code-server/entrypoint.sh
COPY docker/editor/seed/settings.json /opt/code-server/seed/settings.json
# The keybindings seed. ⛔ GENERATED, never hand-edited: it is every
# default keybinding whose command `lockdown/allowed.js` does not allow, and
# `python3 docker/editor/confinement.py --write <image>...` rewrites it from a
# real workbench, one image per runtime set it serves (`SEED_SETS`). No
# `@runtime` block and no placeholder -- a keybinding names no toolchain -- so
# nothing is substituted into it below.
# ⚠️ It is SEEDED rather than applied at run time because the workbench reads
# the user keybindings file when a SESSION STARTS and ignores a write made
# while one is open: measured, and `lockdown/keybindings.js` carries it.
COPY docker/editor/seed/keybindings.json /opt/code-server/seed/keybindings.json
RUN for name in ${SEED_DROP}; do \
      sed -i "/\/\/ @runtime $name\$/,/\/\/ @end $name\$/d" /opt/code-server/seed/settings.json; \
    done \
 && sed -i "s/@JAVA_RUNTIME@/${JAVA_RUNTIME}/" /opt/code-server/seed/settings.json \
 && ! grep -q '@JAVA_RUNTIME@' /opt/code-server/seed/settings.json \
 && chmod 755 /opt/code-server/entrypoint.sh \
 && chmod 644 /opt/code-server/seed/settings.json /opt/code-server/seed/keybindings.json

# Named volumes are initialised from the image. Docker creates a mount point
# that does not exist in the image owned by root, and code-server then cannot
# write its own config (EACCES on /home/coder/.config). So each is created here,
# owned by the runtime uid. (`.m2` holds the Maven repository the prime seeds.)
RUN install -d -o 1000 -g 1000 /home/coder/.config /home/coder/.local /home/coder/.gradle /home/coder/.m2

# ----------------------------------------------------------- the prime
# The build-time warm cache: a consumer's prime directory (`build.py --prime`)
# arrives as the read-only named context `consumer-prime`, and each warmer in
# `prime/` builds its project with the network into a seed under
# /opt/code-server/prime. Without a prime the context is empty and both
# switches are `no`. ⭐ PRIME_KEY is the prime's digest, so the warm is cached
# per prime and never served to another. ⛔ A prime whose
# build compiles or tests nothing fails here, naming what it lacked: it would
# prime nothing while appearing to succeed. The version guards ran before
# Docker started (`prime/prime.py`, from pins.json).
ARG WITH_GRADLE_PRIME
ARG WITH_MAVEN_PRIME
ARG PRIME_KEY
RUN --mount=type=bind,from=consumer-prime,target=/tmp/prime \
    --mount=type=bind,source=prime,target=/tmp/warmers \
    set -eu; \
    echo "prime: ${PRIME_KEY}"; \
    if [ "${WITH_GRADLE_PRIME}" = yes ]; then \
      sh /tmp/warmers/warm-gradle.sh warm /tmp/prime/gradle /opt/code-server/prime/gradle-home; \
    fi; \
    if [ "${WITH_MAVEN_PRIME}" = yes ]; then \
      sh /tmp/warmers/warm-maven.sh warm /tmp/prime/maven /opt/code-server/prime/maven-repo; \
    fi; \
    rm -rf /root/.m2 /root/.local/share/kotlin /tmp/kotlin-daemon*

# ⭐ What the image guarantees, checked before it is tagged: a FRESH copy of
# the prime builds with NO network from a copy of the seed. An image whose
# seed would miss offline is never tagged.
RUN --network=none --mount=type=bind,from=consumer-prime,target=/tmp/prime \
    --mount=type=bind,source=prime,target=/tmp/warmers \
    set -eu; \
    if [ "${WITH_GRADLE_PRIME}" = yes ]; then \
      sh /tmp/warmers/warm-gradle.sh prove /tmp/prime/gradle /opt/code-server/prime/gradle-home; \
    fi; \
    if [ "${WITH_MAVEN_PRIME}" = yes ]; then \
      sh /tmp/warmers/warm-maven.sh prove /tmp/prime/maven /opt/code-server/prime/maven-repo; \
    fi; \
    rm -rf /root/.m2 /root/.local/share/kotlin /tmp/kotlin-daemon*

# ⛔ /opt holds exactly the declared set's trees plus code-server's, JAVA_HOME
# is set exactly when java is declared, every /opt entry on PATH exists, and
# every toolchain reports the version the pins record — under `sh -c` (what
# `docker exec` gets) AND under `bash -lc` (what the integrated terminal gets).
# An image that fails any of them is not tagged. ⭐ What each toolchain
# reported is kept in /opt/code-server/toolchain-versions.
ARG CHECKS
ARG OPT_EXPECTED
ARG WITH_JAVA
ARG JAVA_HOME_DIR
RUN set -eu; \
    held="$(LC_ALL=C ls -A /opt | xargs)"; \
    [ "$held" = "${OPT_EXPECTED}" ] \
      || { echo "/opt holds '$held'; the declared set places '${OPT_EXPECTED}'" >&2; exit 1; }; \
    if [ "${WITH_JAVA}" = yes ]; then want="${JAVA_HOME_DIR}"; else want=unset; fi; \
    : > /opt/code-server/toolchain-versions; \
    for shell in "sh -c" "bash -lc"; do \
      got="$(HOME=/tmp/check-home $shell 'printf %s "${JAVA_HOME-unset}"')"; \
      [ "$got" = "$want" ] \
        || { echo "JAVA_HOME is '$got' under $shell; the declared set gives '$want'" >&2; exit 1; }; \
      for entry in $(HOME=/tmp/check-home $shell 'printf %s "$PATH"' | tr ':' ' '); do \
        case "$entry" in /opt/*) [ -d "$entry" ] \
          || { echo "PATH names $entry under $shell, which the declared set does not place" >&2; exit 1; } ;; \
        esac; \
      done; \
      printf '%s\n' "${CHECKS}" | while IFS='|' read -r name command expect; do \
        [ -n "$name" ] || continue; \
        out="$(HOME=/tmp/check-home $shell "$command" 2>&1)" \
          || { echo "$name: '$command' failed under $shell" >&2; exit 1; }; \
        printf '%s' "$out" | grep -qF -- "$expect" \
          || { echo "$name: '$command' does not report '$expect' under $shell" >&2; exit 1; }; \
        echo "$shell: $name: $expect" | tee -a /opt/code-server/toolchain-versions; \
      done; \
    done; \
    chmod 644 /opt/code-server/toolchain-versions; \
    rm -rf /tmp/check-home

ARG DECLARED
ARG RUNNER_IMAGE
LABEL org.studyforge.editor.runtimes="${DECLARED}" \
      org.studyforge.editor.runner="${RUNNER_IMAGE}"
USER 1000

# The base bakes `--bind-addr 0.0.0.0:8080 .` into its ENTRYPOINT, so a compose
# `command:` would be APPENDED after those arguments. Both are re-declared: the
# entrypoint takes no arguments of its own, and CMD is the whole code-server
# command line, which a compose file replaces outright (its run shape is `consuming.json`'s).
#
# ⛔ `--disable-workspace-trust` IS LOAD-BEARING AND IS NOT A PREFERENCE.
# ⚠️ A corpus's sources arrive as a bind mount, which is an UNTRUSTED folder,
# and a workbench in Restricted Mode DISABLES every extension that does not
# declare support -- with no error and no log line. Measured on code-server
# 4.137.0: the lockdown was installed, listed by `--list-extensions` and
# present in `extensions.json`, and the extension host activated it in NO
# session; `vscjava.vscode-java-debug`, `vscjava.vscode-java-test` and
# `vscode.git` were disabled in the same sessions. ⛔ The contract carried the
# flag in `consuming.json`'s `editor.command` and THIS LINE DID NOT, so every
# consumer that ran the image with `docker run` -- which is how the reader's
# own editor was started -- got a workbench with no lockdown at all.
# ⭐ The manifest's `capabilities.untrustedWorkspaces` is the other half: this
# flag covers a consumer who does not replace the command, that declaration
# covers a consumer who does.
ENTRYPOINT ["/opt/code-server/entrypoint.sh"]
CMD ["--bind-addr", "0.0.0.0:8080", "--extensions-dir=/opt/code-server/extensions", "--disable-workspace-trust", "."]
