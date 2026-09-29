r"""Who may talk to the server at all, and what a page it serves may do.

**What it does.** Holds the loopback bind rule, the `Host` allow-list, the
cross-site refusals and the content policy, and answers one question per request:
`refusal(peer, headers)` is `None` for a local same-site request and otherwise the
message the `403` carries.

**How you use it.** `require_loopback(host)` before a socket is bound;
`refusal(peer, headers, allowed_hosts)` before any route runs; `SECURITY_HEADERS`
on every response, error responses included.

**Depends on.** `urllib.parse`. Nothing else.

## ⛔ Four checks, four different attacks

| check | the attack it answers |
|---|---|
| bind is `127.0.0.1` | an edit that exposes an unauthenticated process to the network |
| peer is loopback | *"the bind is correct"* is the assumption an edit breaks silently |
| `Host` is a loopback name | DNS rebinding: `evil.example` resolving to `127.0.0.1` |
| `Sec-Fetch-Site` / `Origin` | a page on another site loading or fetching from this one |

⚠️ **Two absences are accepted, and each is a decision.** A request with no `Host`
is a hand-rolled client and not a browser, so there is no rebinding to defend
against. `Origin: null` is what a page opened from `file://` sends (R8), and a
sandboxed frame on another site that also sends it still carries
`Sec-Fetch-Site: cross-site`, which is refused.

## ⚠️ The content policy

`'unsafe-inline'` for scripts and styles is the design and not a concession: a page
that must also work from `file://` cannot rely on a nonce the server mints. What
could reach off the machine is closed instead — nothing beyond `'self'`, no forms,
no plugins, no base rewriting.

## ⛔ Framing is TWO-SIDED, and the two sides get opposite answers

⭐ **`frame-ancestors 'none'` and `X-Frame-Options: DENY` govern this page being
framed BY somebody else, and they stay `'none'` forever** — they are in
`POLICY_TAIL` and `FIXED_HEADERS`, where nothing composes them.
⭐ **`frame-src` governs what this page may EMBED, and it is the ONE directive that
cannot be a constant**: the editor a practice panel frames is published on a
per-project host port that R8 forbids a built page from naming, so it is known
only at serve time. `content_policy(frames)` composes it from the editors an
instance actually discovered; `frames=()` — no editor, or no instance to ask —
is `frame-src 'none'`.
⚠️ **This module composes whatever it is handed and decides nothing about WHEN an
origin stops being handed to it.** ⛔ That is the caller's, and the caller's answer
is this: `serve.routes.runs.Runs.origins()` keeps every origin the instance has
ever discovered, because a policy read through a ten-second cache was `'none'` again
ten seconds after anything asked. ⭐ A `frames` that narrows is still composed
faithfully here, which is what `serve.app`'s own reading asserts.
⛔ **Never a wildcard.** `frame_origin` admits a loopback `http`/`https` origin and
nothing else, so an origin carrying a space, a quote or a `;` — which is how a
second directive would be forged into the header — names nothing; `*` is in
`FORBIDDEN`, so `http://127.0.0.1:*` cannot be smuggled in as an origin either.

⚠️ **Framing has two halves**: `code-server` must not refuse being framed, and
OUR OWN `frame-src` must allow the load. A negative on one side of a two-sided
property is not a negative.
"""

from __future__ import annotations

from collections.abc import Collection, Mapping
from urllib.parse import urlsplit

#: The only address this server binds. ⛔ Never `0.0.0.0`: the process reads the
#: reader's disk and has no authentication anywhere in it.
LOOPBACK = "127.0.0.1"

#: What the published form binds INSIDE its container. ⛔ Never on a host: the
#: `--published` form refuses to start outside a container (`cli/serve.py`).
PUBLISHED_BIND = "0.0.0.0"

#: Peer addresses answered at all.
LOOPBACK_PEERS = frozenset({"127.0.0.1", "::1", "::ffff:127.0.0.1"})

#: `Host` names accepted, with any port removed.
ALLOWED_HOSTS = frozenset({"127.0.0.1", "localhost", "::1", "[::1]"})

#: `Sec-Fetch-Site` values a browser sends for a request that is not cross-site.
SAME_SITE = frozenset({"same-origin", "same-site", "none"})

#: The directives before `frame-src`, which is the only one composed per instance.
POLICY_HEAD = (
    "default-src 'self'",
    "script-src 'self' 'unsafe-inline'",
    "style-src 'self' 'unsafe-inline'",
    "img-src 'self' data:",
    "media-src 'self'",
    # ⭐ `data:` because `render/pageassets/faces.py` embeds every face in `page.css`
    # so `file://` carries them too (R8); `'self'` alone blocked all seven.
    "font-src 'self' data:",
    "connect-src 'self'",
)

#: The directives after it. ⛔ `frame-ancestors 'none'` lives HERE, where no
#: argument reaches it: what this page may embed widens, being embedded never does.
POLICY_TAIL = (
    "object-src 'none'",
    "base-uri 'none'",
    "form-action 'none'",
    "frame-ancestors 'none'",
)

#: The source list that admits nothing at all.
NOTHING = "'none'"

#: ⛔ Characters an origin this policy names may never carry. A space or a comma
#: ends one source, a `;` starts the next directive, a quote forges a keyword, `*`
#: is the wildcard this module forbids outright, and `@` hides a host behind userinfo.
FORBIDDEN = frozenset(" \t\r\n\f\v;,*'\"\\@")


def frame_origin(origin: str, allowed: frozenset[str] = ALLOWED_HOSTS) -> str | None:
    """Return the origin a `frame-src` may name, or `None` for anything else."""
    candidate = origin.strip()
    if not candidate or FORBIDDEN & set(candidate):
        return None
    parts = urlsplit(candidate)
    if parts.scheme not in ("http", "https") or not parts.netloc:
        return None
    if parts.path not in ("", "/") or parts.query or parts.fragment:
        return None
    return f"{parts.scheme}://{parts.netloc}" if host_allowed(parts.netloc, allowed) else None


def frame_source(frames: Collection[str] = (), allowed: frozenset[str] = ALLOWED_HOSTS) -> str:
    """Return the `frame-src` value for these origins: `'none'` until one is admitted."""
    named: list[str] = []
    for origin in frames:
        one = frame_origin(origin, allowed)
        if one is not None and one not in named:
            named.append(one)
    return " ".join(named) if named else NOTHING


def content_policy(frames: Collection[str] = (), allowed: frozenset[str] = ALLOWED_HOSTS) -> str:
    """Return the policy an instance that discovered these editor origins sends."""
    embeddable = f"frame-src {frame_source(frames, allowed)}"
    return "; ".join((*POLICY_HEAD, embeddable, *POLICY_TAIL))


#: What an instance with no editor to frame sends, which is also the floor.
CONTENT_POLICY = content_policy()

#: Every header but the policy. ⛔ `X-Frame-Options: DENY` is the other half of
#: `frame-ancestors` and is likewise never composed.
FIXED_HEADERS = (
    ("X-Content-Type-Options", "nosniff"),
    ("X-Frame-Options", "DENY"),
    ("Referrer-Policy", "no-referrer"),
    ("Cross-Origin-Opener-Policy", "same-origin"),
    ("Cross-Origin-Resource-Policy", "same-origin"),
)


def security_headers(frames: Collection[str] = ()) -> tuple[tuple[str, str], ...]:
    """Return every header a response carries, its policy composed for this instance."""
    return (("Content-Security-Policy", content_policy(frames)), *FIXED_HEADERS)


#: Sent on every response an instance with no editor writes.
SECURITY_HEADERS = security_headers()


def framable(frames: Collection[str], host: str | None = None) -> list[str]:
    """Return the origins a page reached at `host` may frame: every loopback one, or none.

    ⭐ **Any accepted loopback name frames the editor** (`127.0.0.1`, `localhost`,
    `[::1]`), because the editor has no session cookie for the frame to be
    cross-site about (register ruling: the editor carries no password, and its
    loopback bind is its whole access control). ⛔ **What is admitted is never
    composed from the request**: the origins are the operator's or the instance's
    own, each still a loopback `http`/`https` origin (`frame_origin`), and a `host`
    that is not a loopback name (which the gate has already refused) is given none.
    ⚠️ Widening the editor's bind must restore its authentication first, and that
    is the compose file's and the documents' to say.
    """
    if host is not None and not host_allowed(host):
        return []
    admitted: list[str] = []
    for origin in frames:
        named = frame_origin(origin)
        if named is not None and named not in admitted:
            admitted.append(named)
    return admitted


def response_headers(
    frames: Collection[str], host: str | None = None
) -> tuple[tuple[str, str], ...]:
    """Return the headers for a page reached at `host`, its frame policy composed for it."""
    return security_headers(framable(frames, host))


REFUSED_PEER = "this server answers loopback clients only"
REFUSED_HOST = "unexpected Host header"
REFUSED_SITE = "cross-site requests are refused"
REFUSED_ORIGIN = "cross-origin requests are refused"


def require_loopback(host: str, *, published: bool = False) -> None:
    """Raise `ValueError` unless `host` is the one address this server binds.

    ⭐ `published` admits `PUBLISHED_BIND` too, and only the `--published` form
    passes it: that form refuses to start outside a container, where every
    interface is the container's own and the compose file publishes the port on
    `127.0.0.1` alone.
    """
    if published and host == PUBLISHED_BIND:
        return
    if host != LOOPBACK:
        raise ValueError(f"this server binds {LOOPBACK} only")


def host_name(header: str | None) -> str:
    """Return a `Host` value's or an authority's name, with any port removed."""
    host = (header or "").strip()
    if host.startswith("["):
        close = host.find("]")
        return host[: close + 1] if close != -1 else host
    if host.count(":") == 1:
        return host.rsplit(":", 1)[0]
    return host


def host_allowed(header: str | None, allowed: frozenset[str] = ALLOWED_HOSTS) -> bool:
    """Say whether a `Host` value names a loopback host, whatever its port."""
    return True if header is None else host_name(header).lower() in allowed


def origin_allowed(header: str | None, allowed: frozenset[str] = ALLOWED_HOSTS) -> bool:
    """Say whether an `Origin` value is absent, `null`, or a loopback http origin."""
    if header is None or header.strip() == "null":
        return True
    parts = urlsplit(header.strip())
    if parts.scheme not in ("http", "https") or not parts.netloc:
        return False
    return host_allowed(parts.netloc, allowed)


def refusal(
    peer: str,
    headers: Mapping[str, str],
    allowed: frozenset[str] = ALLOWED_HOSTS,
    peers: frozenset[str] | None = LOOPBACK_PEERS,
) -> str | None:
    """Return `None` for a local same-site request, else the refusal's message.

    ⭐ `peers=None` is the published form's (`PUBLISHED_BIND`): inside its own
    container the server's peer is the compose network's gateway, and the
    loopback bind it stands for is the compose file's `127.0.0.1:` port. ⛔ The
    `Host`, `Sec-Fetch-Site` and `Origin` checks are unchanged either way.
    """
    if peers is not None and peer not in peers:
        return REFUSED_PEER
    if not host_allowed(headers.get("Host"), allowed):
        return REFUSED_HOST
    site = (headers.get("Sec-Fetch-Site") or "").strip().lower()
    if site and site not in SAME_SITE:
        return REFUSED_SITE
    if not origin_allowed(headers.get("Origin"), allowed):
        return REFUSED_ORIGIN
    return None
