"""Drive a headless browser: press a key, read the page back.

**What it does.** A Chrome DevTools Protocol client small enough to live in
the standard library: `targets()`/`wait_for_target()` find the page a probe
opened, `Session` speaks the protocol over a WebSocket this module implements
in about forty lines, and `press()`/`click()`/`evaluate()` are the three verbs
a confinement check needs.

**Why it exists, rather than a settings file and a hope.** The lockdown confines the
workbench's command surface, and the only honest way to check a confinement is
to be the reader: press `Ctrl+Shift+P` at a real session and look at what
appears. ⛔ A check that reads the keybindings FILE cannot see what the
workbench ALLOWS -- the same shape of error as the Restricted Mode defect, where a check that read
INSTALLATION could not see ACTIVATION. `docker/editor/activation.py` already
opens a real session with a real browser; this module is the half that can
also TYPE into it.

**How you use it.** It is not a command of its own. `confinement.py` launches
the browser with `--remote-debugging-port`, waits for the workbench's page
target, and drives it:

    page = cdp.wait_for_target(port, "127.0.0.1:8080")
    with cdp.Session(page["webSocketDebuggerUrl"]) as session:
        session.call("Runtime.enable")
        cdp.press(session, "ctrl+shift+p")
        cdp.evaluate(session, "document.querySelectorAll('.quick-input-widget').length")

**Depends on.** The standard library only: `socket` for the WebSocket,
`urllib` for the debugger's HTTP endpoint, `json` and `struct`. ⛔ No
websocket library, no browser driver, no package manager -- this component
builds an image and has never had a dependency it did not pin.

## ⛔ Why a WebSocket by hand
The debugger's `/json` endpoint lists targets over plain HTTP, but every
command to a target goes over a WebSocket, and nothing in the standard library
speaks one. It is a masked-frame protocol with a four-line handshake; what is
here is the client half of it and nothing more -- no server, no permessage
deflate, no continuation frames beyond what a JSON answer needs.
"""

from __future__ import annotations

import base64
import json
import os
import socket
import struct
import time
import urllib.error
import urllib.parse
import urllib.request


class CdpError(Exception):
    """A browser that could not be driven, or a command it refused."""


#: Frame opcodes this client understands. A `PING` is answered by ignoring it
#: -- the debugger does not require a pong within one short probe -- and a
#: `CLOSE` ends the session.
TEXT, CONTINUATION, CLOSE, PING = 0x1, 0x0, 0x8, 0x9
HANDSHAKE_TIMEOUT = 30.0


def targets(port: int, timeout: float = 10.0) -> list[dict]:
    """Every debuggable target the browser on `port` is offering."""
    try:
        with urllib.request.urlopen(f"http://127.0.0.1:{port}/json", timeout=timeout) as answer:
            return json.loads(answer.read().decode("utf-8"))
    except (urllib.error.URLError, OSError, TimeoutError, ValueError) as error:
        raise CdpError(f"the browser's debugger on 127.0.0.1:{port} did not answer: {error}") from None


def wait_for_target(port: int, needle: str, timeout: float = 90.0) -> dict:
    """The page target whose URL holds `needle`, once the browser has opened it."""
    deadline, seen = time.monotonic() + timeout, []
    while time.monotonic() < deadline:
        try:
            seen = targets(port)
        except CdpError:
            seen = []
        for target in seen:
            if target.get("type") == "page" and needle in target.get("url", ""):
                return target
        time.sleep(1.0)
    raise CdpError(f"no page target whose url holds {needle!r} within {timeout:.0f}s; "
                   f"the browser was showing {[target.get('url') for target in seen]}")


class Session:
    """One WebSocket to one debugger target, speaking the protocol's request/answer half."""

    def __init__(self, url: str, timeout: float = HANDSHAKE_TIMEOUT):
        parts = urllib.parse.urlsplit(url)
        self.timeout = timeout
        self.socket = socket.create_connection((parts.hostname, parts.port or 80), timeout=timeout)
        self.socket.settimeout(timeout)
        self.buffer = b""
        self.last = 0
        self._handshake(parts)

    def __enter__(self) -> Session:
        return self

    def __exit__(self, *_) -> None:
        self.close()

    def _handshake(self, parts) -> None:
        key = base64.b64encode(os.urandom(16)).decode()
        path = parts.path + (f"?{parts.query}" if parts.query else "")
        self.socket.sendall(
            f"GET {path} HTTP/1.1\r\nHost: {parts.hostname}:{parts.port}\r\n"
            f"Upgrade: websocket\r\nConnection: Upgrade\r\n"
            f"Sec-WebSocket-Key: {key}\r\nSec-WebSocket-Version: 13\r\n\r\n".encode())
        while b"\r\n\r\n" not in self.buffer:
            self.buffer += self._chunk()
        head, _, self.buffer = self.buffer.partition(b"\r\n\r\n")
        status = head.split(b"\r\n", 1)[0]
        if b" 101" not in status:
            raise CdpError(f"the debugger refused the websocket upgrade: {status.decode('utf-8', 'replace')}")

    def _chunk(self) -> bytes:
        try:
            chunk = self.socket.recv(65536)
        except (TimeoutError, socket.timeout) as error:
            raise CdpError(f"the debugger sent nothing for {self.timeout:.0f}s") from error
        if not chunk:
            raise CdpError("the debugger closed the connection")
        return chunk

    def _take(self, count: int) -> bytes:
        while len(self.buffer) < count:
            self.buffer += self._chunk()
        taken, self.buffer = self.buffer[:count], self.buffer[count:]
        return taken

    def send(self, text: str) -> None:
        """One masked text frame. ⛔ A client frame is always masked; a server rejects an unmasked one."""
        body = text.encode("utf-8")
        header = bytearray([0x80 | TEXT])
        if len(body) < 126:
            header.append(0x80 | len(body))
        elif len(body) < 1 << 16:
            header.append(0x80 | 126)
            header += struct.pack(">H", len(body))
        else:
            header.append(0x80 | 127)
            header += struct.pack(">Q", len(body))
        mask = os.urandom(4)
        header += mask
        self.socket.sendall(bytes(header) + bytes(byte ^ mask[i % 4] for i, byte in enumerate(body)))

    def _frame(self) -> tuple[int, bytes]:
        first, second = self._take(2)
        opcode, length = first & 0x0F, second & 0x7F
        if length == 126:
            length = struct.unpack(">H", self._take(2))[0]
        elif length == 127:
            length = struct.unpack(">Q", self._take(8))[0]
        return opcode, self._take(length)

    def message(self) -> dict:
        """The next protocol message, skipping frames that carry none."""
        while True:
            opcode, payload = self._frame()
            if opcode == CLOSE:
                raise CdpError("the debugger closed the connection")
            if opcode in (TEXT, CONTINUATION):
                return json.loads(payload.decode("utf-8"))
            if opcode != PING:
                raise CdpError(f"the debugger sent an opcode this client does not read: {opcode}")

    def call(self, method: str, params: dict | None = None, timeout: float = 30.0) -> dict:
        """Send one command and return its result, ignoring the events that arrive meanwhile."""
        self.last += 1
        wanted = self.last
        self.send(json.dumps({"id": wanted, "method": method, "params": params or {}}))
        deadline = time.monotonic() + timeout
        while time.monotonic() < deadline:
            answer = self.message()
            if answer.get("id") != wanted:
                continue
            if "error" in answer:
                raise CdpError(f"{method}: {answer['error']}")
            return answer.get("result", {})
        raise CdpError(f"{method}: the browser did not answer within {timeout:.0f}s")

    def close(self) -> None:
        try:
            self.socket.close()
        except OSError:
            pass


def evaluate(session: Session, expression: str, timeout: float = 30.0):
    """Run `expression` in the page and return its value.

    ⛔ An exception inside the page is RAISED here rather than answered as
    `None`: a check whose probe silently failed reads as "nothing was open".
    """
    result = session.call("Runtime.evaluate", {"expression": expression,
                                               "returnByValue": True, "awaitPromise": True}, timeout)
    if result.get("exceptionDetails"):
        raise CdpError(f"the page raised evaluating a probe: {result['exceptionDetails'].get('text')}")
    return result.get("result", {}).get("value")


#: The modifier bits the protocol takes, which are not the browser's own.
MODIFIERS = {"alt": 1, "ctrl": 2, "meta": 4, "shift": 8}
#: Keys whose `code` and virtual key code are not derived from the letter.
#: ⚠️ VS Code's chord spelling on the left, the browser's on the right.
NAMED = {
    "escape": ("Escape", 27), "enter": ("Enter", 13), "space": ("Space", 32),
    "tab": ("Tab", 9), "backspace": ("Backspace", 8), "delete": ("Delete", 46),
    "home": ("Home", 36), "end": ("End", 35), "pageup": ("PageUp", 33), "pagedown": ("PageDown", 34),
    "up": ("ArrowUp", 38), "down": ("ArrowDown", 40), "left": ("ArrowLeft", 37), "right": ("ArrowRight", 39),
    "`": ("Backquote", 192), ",": ("Comma", 188), ".": ("Period", 190), "/": ("Slash", 191),
    "[": ("BracketLeft", 219), "]": ("BracketRight", 221), "-": ("Minus", 189), "=": ("Equal", 187),
}


def key_event(chord: str) -> dict:
    """Translate a VS Code chord such as `ctrl+shift+p` into one protocol key event.

    ⛔ Single chords only. A two-part chord (`ctrl+k ctrl+s`) is two presses
    and the caller sends them as two, so nothing here has to model chord
    state -- the workbench does.
    """
    parts = chord.lower().split("+")
    name, modifiers = parts[-1], sum(MODIFIERS.get(part, 0) for part in parts[:-1])
    if name in NAMED:
        code, number = NAMED[name]
        key = code if code not in ("Space",) else " "
    elif len(name) > 1 and name[0] == "f" and name[1:].isdigit():
        number = 111 + int(name[1:])
        code = key = name.upper()
    elif len(name) == 1 and name.isalnum():
        code = f"Key{name.upper()}" if name.isalpha() else f"Digit{name}"
        number = ord(name.upper())
        key = name.upper() if modifiers & MODIFIERS["shift"] else name
    else:
        raise CdpError(f"this client cannot spell the key {name!r} of the chord {chord!r}")
    return {"modifiers": modifiers, "code": code, "key": key,
            "windowsVirtualKeyCode": number, "nativeVirtualKeyCode": number}


def press(session: Session, chord: str) -> None:
    """Press and release one chord at whatever has focus."""
    event = key_event(chord)
    session.call("Input.dispatchKeyEvent", {"type": "rawKeyDown", **event})
    session.call("Input.dispatchKeyEvent", {"type": "keyUp", **event})


def click(session: Session, x: int, y: int) -> None:
    """One left click, which is how focus is put back into the editor between presses."""
    for kind in ("mousePressed", "mouseReleased"):
        session.call("Input.dispatchMouseEvent",
                     {"type": kind, "x": x, "y": y, "button": "left", "clickCount": 1})


def type_text(session: Session, text: str) -> None:
    """Insert text as the reader would, without modelling a keystroke per character."""
    session.call("Input.insertText", {"text": text})
