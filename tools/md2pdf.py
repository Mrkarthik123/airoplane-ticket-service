#!/usr/bin/env python3
"""Render a Markdown file to a print-ready HTML page for Chrome headless PDF export."""
import re
import sys
import pathlib
import markdown

src = pathlib.Path(sys.argv[1])
out = pathlib.Path(sys.argv[2])
title = sys.argv[3] if len(sys.argv) > 3 else src.stem

body = src.read_text(encoding="utf-8")

html_body = markdown.markdown(
    body,
    extensions=["extra", "sane_lists", "admonition", "nl2br"],
    output_format="html5",
)

# drop the empty header row produced by "| | |" title blocks
html_body = re.sub(
    r"<thead>\s*<tr>\s*(?:<th>\s*</th>\s*)+</tr>\s*</thead>",
    "",
    html_body,
)

css = """
@page { size: A4; margin: 18mm 16mm 16mm 16mm; }

html { -webkit-print-color-adjust: exact; print-color-adjust: exact; }

body {
  font-family: "Georgia", "Times New Roman", serif;
  font-size: 11pt;
  line-height: 1.5;
  color: #16181d;
  margin: 0;
  counter-reset: fig;
}

h1 {
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  font-size: 20pt;
  letter-spacing: 0.6px;
  text-align: center;
  margin: 0 0 4pt 0;
  color: #0b3d91;
  border: none;
}

h1 + h3 {
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  font-size: 11.5pt;
  font-weight: 500;
  font-style: italic;
  text-align: center;
  color: #4a5568;
  margin: 0 0 18pt 0;
}

h2 {
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  font-size: 13pt;
  color: #0b3d91;
  margin: 20pt 0 8pt 0;
  padding-bottom: 3pt;
  border-bottom: 1.2pt solid #c3d2ea;
  page-break-after: avoid;
}

h3 {
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  font-size: 11pt;
  font-weight: 600;
  color: #1f2937;
  margin: 13pt 0 5pt 0;
  page-break-after: avoid;
}

p { margin: 0 0 7pt 0; text-align: justify; orphans: 3; widows: 3; }

strong { color: #0b3d91; }
em { color: #334155; }

hr { border: none; border-top: 0.6pt solid #d6dee9; margin: 14pt 0; }

ul, ol { margin: 0 0 8pt 0; padding-left: 18pt; }
li { margin-bottom: 3pt; text-align: justify; }

table {
  width: 100%;
  border-collapse: collapse;
  margin: 6pt 0 12pt 0;
  font-size: 9.6pt;
  page-break-inside: avoid;
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
}
th {
  background: #0b3d91;
  color: #ffffff;
  text-align: left;
  font-weight: 600;
  padding: 5pt 6pt;
  border: 0.5pt solid #0b3d91;
}
td {
  padding: 4.5pt 6pt;
  border: 0.5pt solid #c3ccd9;
  vertical-align: top;
}
tr:nth-child(even) td { background: #f4f7fc; }

pre {
  background: #f5f7fa;
  border: 0.5pt solid #d3dae4;
  border-left: 2.5pt solid #0b3d91;
  border-radius: 2pt;
  padding: 7pt 9pt;
  overflow: hidden;
  font-family: "SF Mono", "Menlo", "Consolas", monospace;
  font-size: 8.2pt;
  line-height: 1.42;
  page-break-inside: avoid;
  white-space: pre-wrap;
  word-wrap: break-word;
}
pre code {
  background: none;
  border: none;
  padding: 0;
  font-size: 8.2pt;
  color: #1f2937;
  white-space: pre-wrap;
}

code {
  font-family: "SF Mono", "Menlo", "Consolas", monospace;
  font-size: 9pt;
  background: #eef2f7;
  color: #0b3d91;
  padding: 0.5pt 2.5pt;
  border-radius: 2pt;
}

blockquote {
  margin: 8pt 0 8pt 10pt;
  padding-left: 9pt;
  border-left: 2pt solid #c3d2ea;
  color: #475569;
  font-style: italic;
}

a { color: #0b3d91; text-decoration: none; }

table:first-of-type { margin-top: 4pt; }
th:empty { background: #eef2f7; border-color: #c3ccd9; }
h2 { page-break-before: auto; }
"""

doc = f"""<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>{title}</title>
<style>{css}</style>
</head>
<body>
{html_body}
</body>
</html>
"""

out.write_text(doc, encoding="utf-8")
print(f"HTML written: {out}  ({len(doc)} bytes)")
