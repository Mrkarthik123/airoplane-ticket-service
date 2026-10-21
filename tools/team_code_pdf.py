#!/usr/bin/env python3
"""Build a complete source-code PDF for the Airoplane Ticket Service project.

Collects every hand-written source file, groups them into logical sections,
renders each file with line numbers and light syntax highlighting, and writes
print-ready HTML that Chrome converts to PDF.

Usage:  python3 team_code_pdf.py <project-root> <output.html>
"""
import html
import pathlib
import re
import sys

ROOT = pathlib.Path(sys.argv[1]).resolve()
OUT = pathlib.Path(sys.argv[2]).resolve()

SECTIONS = [
    ("Part 1 — Desktop Application and HTTP Server (Java 17)", [
        "src/main/java/com/airline/App.java",
        "src/main/java/com/airline/model/Flight.java",
        "src/main/java/com/airline/model/User.java",
        "src/main/java/com/airline/model/Booking.java",
        "src/main/java/com/airline/service/AuthService.java",
        "src/main/java/com/airline/service/FlightService.java",
        "src/main/java/com/airline/service/BookingService.java",
        "src/main/java/com/airline/store/DataStore.java",
        "src/main/java/com/airline/util/Json.java",
        "src/main/java/com/airline/util/HttpUtil.java",
        "src/main/java/com/airline/util/Passwords.java",
        "src/main/java/com/airline/ui/DesktopLauncher.java",
        "src/main/java/com/airline/ui/MainFrame.java",
        "src/main/java/com/airline/ui/LoginDialog.java",
        "src/main/java/com/airline/ui/Theme.java",
        "src/main/java/com/airline/ui/Soft.java",
        "src/main/java/com/airline/ui/SoftPanel.java",
        "src/main/java/com/airline/ui/SoftButton.java",
        "src/main/java/com/airline/ui/SoftTextField.java",
        "src/main/java/com/airline/ui/SoftPasswordField.java",
        "src/main/java/com/airline/ui/SoftComboUI.java",
        "src/main/java/com/airline/ui/SoftTabbedPaneUI.java",
        "src/main/java/com/airline/ui/PlaceholderField.java",
        "src/main/java/com/airline/ui/PlaceholderPasswordField.java",
        "src/main/java/com/airline/ui/SkyPanel.java",
        "src/main/java/com/airline/ui/SearchPanel.java",
        "src/main/java/com/airline/ui/AirportsPanel.java",
        "src/main/java/com/airline/ui/BookingDialog.java",
        "src/main/java/com/airline/ui/BookingsPanel.java",
        "src/main/java/com/airline/ui/TicketDialog.java",
        "src/main/java/com/airline/ui/AdminPanel.java",
    ]),
    ("Part 2 — Web and Progressive Web App Front End", [
        "src/main/resources/static/index.html",
        "src/main/resources/static/styles.css",
        "src/main/resources/static/app.js",
        "src/main/resources/static/manifest.webmanifest",
        "src/main/resources/static/sw.js",
    ]),
    ("Part 3 — Build, Packaging and Run Scripts", [
        "pom.xml",
        "run.sh",
        "AiroplaneTicketService.command",
        "mobile/package.json",
        "mobile/capacitor.config.js",
        "mobile/build-apk.sh",
        "mobile/tools/IconGen.java",
    ]),
    ("Part 4 — Android Client (Capacitor + Web Assets)", [
        "mobile/www/index.html",
        "mobile/www/styles.css",
        "mobile/www/app.js",
        "mobile/www/manifest.webmanifest",
        "mobile/android/app/src/main/java/in/skyjet/app/MainActivity.java",
        "mobile/android/app/src/main/AndroidManifest.xml",
        "mobile/android/app/src/main/assets/capacitor.config.json",
        "mobile/android/app/src/main/res/layout/activity_main.xml",
        "mobile/android/app/src/main/res/values/strings.xml",
        "mobile/android/app/src/main/res/values/styles.xml",
        "mobile/android/app/src/main/res/xml/config.xml",
        "mobile/android/app/build.gradle",
        "mobile/android/build.gradle",
        "mobile/android/variables.gradle",
        "mobile/android/settings.gradle",
        "mobile/android/app/src/main/res/drawable/ic_launcher_background.xml",
        "mobile/android/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml",
        "mobile/android/app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml",
        "mobile/android/app/src/main/res/drawable-v24/ic_launcher_foreground.xml",
    ]),
    ("Part 5 — Seed Data and Project Documentation", [
        "data/flights.json",
        "data/users.json",
        "README.md",
    ]),
]

LANG = {
    ".java": "Java", ".js": "JavaScript", ".html": "HTML", ".css": "CSS",
    ".json": "JSON", ".xml": "XML", ".sh": "Shell", ".md": "Markdown",
    ".command": "Shell", ".webmanifest": "JSON", ".gradle": "Gradle",
    ".pro": "ProGuard",
}

KEYWORDS = {
    "java": {"abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
             "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
             "finally", "float", "for", "if", "implements", "import", "instanceof", "int",
             "interface", "long", "native", "new", "package", "private", "protected", "public",
             "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
             "throw", "throws", "transient", "try", "void", "volatile", "while", "var", "record"},
    "js": {"async", "await", "break", "case", "catch", "const", "continue", "default", "delete",
           "do", "else", "export", "extends", "function", "if", "import", "in", "instanceof",
           "let", "new", "of", "return", "static", "super", "switch", "this", "throw", "try",
           "typeof", "var", "void", "while", "yield", "true", "false", "null"},
}

C_KEYWORDS = {"int", "char", "float", "double", "void", "return", "if", "else", "for", "while",
              "struct", "typedef", "static", "const", "unsigned", "long", "short"}


def language_of(path: pathlib.Path) -> str:
    suffix = path.suffix.lower()
    if suffix in (".java", ".js", ".html", ".css", ".json", ".xml", ".sh", ".command", ".md", ".pro", ".gradle"):
        return suffix.lstrip(".")
    return "txt"


def tokenize(text: str, lang: str):
    """Split a file into lines of (text, css_class) segments.

    The scanner keeps state across line breaks, so a block comment that spans
    several lines is highlighted as a comment throughout and a string is never
    allowed to run past the end of its line.
    """
    keywords = KEYWORDS.get(lang, set())
    if lang in ("java", "js"):
        keywords = keywords | {"true", "false", "null"}
    if lang == "c":
        keywords = C_KEYWORDS

    lines: list[list[tuple[str, str | None]]] = [[]]
    current = lines[0]
    i, n = 0, len(text)
    in_block = False

    def close_line() -> None:
        nonlocal current
        current = []
        lines.append(current)

    while i < n:
        ch = text[i]

        if in_block:
            end = text.find("*/", i)
            newline_at = text.find("\n", i)
            if end != -1 and (newline_at == -1 or end < newline_at):
                current.append((text[i:end + 2], "cm"))
                i = end + 2
                in_block = False
            elif newline_at == -1:
                current.append((text[i:], "cm"))
                i = n
            else:
                current.append((text[i:newline_at], "cm"))
                close_line()
                i = newline_at + 1
            continue

        if ch == "\n":
            close_line()
            i += 1
            continue

        if text.startswith("//", i) and lang not in ("html", "css"):
            stop = text.find("\n", i)
            stop = n if stop == -1 else stop
            current.append((text[i:stop], "cm"))
            i = stop
            continue

        if text.startswith("/*", i) and lang not in ("html", "css"):
            current.append(("/*", "cm"))
            i += 2
            in_block = True
            continue

        if lang == "html" and text.startswith("<!--", i):
            end = text.find("-->", i)
            if end == -1:
                stop = text.find("\n", i)
                stop = n if stop == -1 else stop
                current.append((text[i:stop], "cm"))
                i = stop
            else:
                current.append((text[i:end + 3], "cm"))
                i = end + 3
            continue

        if ch in ('"', "'") and lang != "html":
            j = i + 1
            while j < n:
                if text[j] == "\\":
                    j += 2
                    continue
                if text[j] == ch or text[j] == "\n":
                    j += 1
                    break
                j += 1
            current.append((text[i:j], "st"))
            i = j
            continue

        if ch.isalpha() or ch == "_":
            j = i
            while j < n and (text[j].isalnum() or text[j] == "_"):
                j += 1
            word = text[i:j]
            if word in keywords:
                current.append((word, "kw"))
            elif lang == "java" and word[0].isupper() and len(word) > 1:
                current.append((word, "ty"))
            else:
                current.append((word, None))
            i = j
            continue

        if ch.isdigit():
            j = i
            while j < n and (text[j].isdigit() or text[j] in "._xXabcdefABCDEF"):
                j += 1
            current.append((text[i:j], "nu"))
            i = j
            continue

        current.append((ch, None))
        i += 1

    return lines


CSS = """
@page { size: A4; margin: 16mm 14mm 14mm 14mm; }
html { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
body {
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  color: #16181d;
  margin: 0;
  font-size: 10pt;
}

.cover { text-align: center; padding-top: 54pt; page-break-after: always; }
.cover .team {
  font-size: 30pt; font-weight: 700; letter-spacing: 3px; color: #0b3d91; margin: 0 0 6pt;
}
.cover .rule { width: 150pt; height: 2.5pt; background: #0b3d91; margin: 14pt auto; }
.cover .proj { font-size: 16pt; font-weight: 600; color: #1f2937; margin: 0 0 4pt; }
.cover .sub { font-size: 11pt; color: #55627a; margin: 0 0 20pt; }
.cover table { width: 78%; margin: 0 auto 18pt; border-collapse: collapse; font-size: 9.5pt; }
.cover td { border-bottom: 0.5pt solid #c3ccd9; padding: 5pt 6pt; text-align: left; }
.cover td.k { color: #55627a; width: 38%; }
.cover .stats { margin: 0 auto; font-size: 9.5pt; color: #334155; }
.cover .stats b { color: #0b3d91; font-size: 13pt; }

.toc h1 { font-size: 17pt; color: #0b3d91; border-bottom: 1.5pt solid #0b3d91; padding-bottom: 4pt; margin: 0 0 10pt; }
.toc .part { font-weight: 700; color: #0b3d91; font-size: 10.5pt; margin: 11pt 0 4pt; }
.toc ol { margin: 0 0 0 26pt; padding: 0; }
.toc li { font-family: "SF Mono", Menlo, monospace; font-size: 8.6pt; margin-bottom: 1.5pt; color: #1f2937; }
.toc li span { color: #64748b; }
.toc .pagebreak { page-break-after: always; }

h2.part {
  font-size: 12.5pt; color: #ffffff; background: #0b3d91;
  padding: 6pt 9pt; margin: 0 0 10pt; border-radius: 2pt;
  page-break-after: avoid;
}
table.code {
  width: 100%; border-collapse: collapse; margin: 0 0 13pt;
  font-family: "SF Mono", Menlo, Consolas, monospace; font-size: 7.6pt; line-height: 1.38;
  table-layout: fixed;
}
table.code thead th {
  background: #eef2f7; color: #0b3d91; text-align: left; font-weight: 600;
  padding: 3.5pt 6pt; border: 0.5pt solid #c3ccd9; font-size: 8pt;
}
table.code thead th.ln { text-align: right; color: #9aa6b8; }
table.code td { padding: 0 6pt; border: none; vertical-align: top; }
table.code tr { page-break-inside: avoid; }
table.code td.ln {
  width: 26pt; text-align: right; color: #9aa6b8; background: #fafbfd;
  border-right: 0.5pt solid #e3e8ef; user-select: none;
}
table.code td.cd {
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  word-break: break-all;
  color: #1f2937;
}
tr.alt td.cd { background: #fbfcfe; }
tr.alt td.ln { background: #f4f6fa; }
.cm { color: #7d8ea3; font-style: italic; }
.st { color: #0a7d55; }
.kw { color: #9333c8; font-weight: 600; }
.ty { color: #0b6fb8; }
.nu { color: #b45309; }
"""


def main() -> None:
    files = []
    for _, paths in SECTIONS:
        for rel in paths:
            path = ROOT / rel
            if path.is_file():
                files.append((rel, path))
            else:
                print(f"  ! missing, skipped: {rel}")

    total_lines = 0
    per_lang = {}
    for rel, path in files:
        raw = path.read_text(encoding="utf-8", errors="replace")
        count = len(raw.splitlines())
        total_lines += count
        lang = language_of(path)
        per_lang[lang] = per_lang.get(lang, 0) + count

    blocks = []
    for part_title, _ in SECTIONS:
        part_files = [f for f in files if f[0] in set(_)]
        if not part_files:
            continue
        blocks.append(f'<h2 class="part">{html.escape(part_title)}</h2>')
        for rel, path in part_files:
            raw = path.read_text(encoding="utf-8", errors="replace")
            lang = language_of(path)
            lines = tokenize(raw, lang)
            rows = []
            for number, segments in enumerate(lines, start=1):
                cls = ' class="alt"' if number % 2 == 0 else ""
                body = "".join(
                    (f'<span class="{klass}">{html.escape(chunk)}</span>' if klass else html.escape(chunk))
                    for chunk, klass in segments
                )
                rows.append(
                    f'<tr{cls}><td class="ln">{number}</td>'
                    f'<td class="cd">{body or " "}</td></tr>'
                )
            blocks.append(
                f'<table class="code">'
                f'<thead><tr><th class="ln" style="width:30pt">#</th>'
                f'<th>{html.escape(rel)} &nbsp;·&nbsp; {html.escape(LANG.get(path.suffix.lower(), lang))} '
                f'&nbsp;·&nbsp; {len(lines)} lines</th></tr></thead>'
                f'<tbody>{"".join(rows)}</tbody></table>'
            )

    by_name = {}
    for ext, n in per_lang.items():
        name = LANG.get("." + ext, ext)
        by_name[name] = by_name.get(name, 0) + n
    lang_stats = " &nbsp;·&nbsp; ".join(
        f"{name} <b>{n:,}</b> lines" for name, n in sorted(by_name.items(), key=lambda x: -x[1])
    )

    toc = ['<div class="toc"><h1>Table of contents</h1>']
    for part_title, paths in SECTIONS:
        present = [p for p in paths if (ROOT / p).is_file()]
        if not present:
            continue
        toc.append(f'<div class="part">{html.escape(part_title)}</div><ol>')
        for rel in present:
            count = len((ROOT / rel).read_text(encoding="utf-8", errors="replace").splitlines())
            toc.append(f'<li>{html.escape(rel)} <span>— {count} lines</span></li>')
        toc.append("</ol>")
    toc.append('<div class="pagebreak"></div></div>')

    cover = f"""
<div class="cover">
  <p class="team">TEAM SEVEN</p>
  <div class="rule"></div>
  <p class="proj">Airoplane Ticket Service</p>
  <p class="sub">Flight Reservation &amp; E-Ticket System &nbsp;·&nbsp; Complete Source Code</p>
  <table>
    <tr><td class="k">Project Title</td><td>Airoplane Ticket Service — Flight Reservation &amp; E-Ticket System</td></tr>
    <tr><td class="k">Team Name</td><td>Team Seven</td></tr>
    <tr><td class="k">Team Members</td><td>1. ____________________ &nbsp; 2. ____________________ &nbsp; 3. ____________________</td></tr>
    <tr><td class="k">Guide / Faculty</td><td>____________________</td></tr>
    <tr><td class="k">Department</td><td>Computer Science &amp; Engineering</td></tr>
    <tr><td class="k">College / Institution</td><td>____________________</td></tr>
    <tr><td class="k">Academic Year</td><td>2025 – 2026</td></tr>
    <tr><td class="k">Date of Submission</td><td>____________________</td></tr>
    <tr><td class="k">Repository</td><td>github.com/Mrkarthik123/airoplane-ticket-service</td></tr>
  </table>
  <p class="stats">
    <b>{len(files)}</b> source files &nbsp;·&nbsp; <b>{total_lines:,}</b> lines of code<br/>
    {lang_stats}
  </p>
</div>
"""

    doc = f"""<!DOCTYPE html>
<html lang="en"><head><meta charset="utf-8">
<title>Team Seven - Airoplane Ticket Service Source Code</title>
<style>{CSS}</style></head>
<body>
{cover}
{"".join(toc)}
{"".join(blocks)}
</body></html>
"""
    OUT.write_text(doc, encoding="utf-8")
    print(f"{len(files)} files, {total_lines:,} lines -> {OUT}")


if __name__ == "__main__":
    main()
