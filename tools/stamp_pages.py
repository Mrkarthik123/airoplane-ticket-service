#!/usr/bin/env python3
"""Stamp 'Page X of Y' and a running footer onto a PDF produced by Chrome.

Usage:  python3 stamp_pages.py input.pdf output.pdf "footer text"
Requires pypdf and reportlab (install in a throwaway venv, e.g. /tmp/pdfvenv).
"""
import sys
from io import BytesIO

from pypdf import PdfReader, PdfWriter
from reportlab.lib.pagesizes import A4
from reportlab.pdfgen import canvas


def main() -> None:
    src, dst = sys.argv[1], sys.argv[2]
    footer = sys.argv[3] if len(sys.argv) > 3 else ""

    reader = PdfReader(src)
    total = len(reader.pages)
    writer = PdfWriter()

    for index, page in enumerate(reader.pages, start=1):
        width = float(page.mediabox.width)
        height = float(page.mediabox.height)

        packet = BytesIO()
        overlay = canvas.Canvas(packet, pagesize=(width, height))
        overlay.setFont("Helvetica", 7.5)
        overlay.setFillGray(0.42)

        label = f"{footer}   |   Page {index} of {total}" if footer else f"Page {index} of {total}"
        overlay.drawCentredString(width / 2, 26, label)
        overlay.setStrokeGray(0.8)
        overlay.setLineWidth(0.4)
        overlay.line(48, 36, width - 48, 36)

        overlay.save()
        packet.seek(0)

        page.merge_page(PdfReader(packet).pages[0])
        writer.add_page(page)

    writer.add_metadata({
        "/Title": "Airoplane Ticket Service - Project Abstract",
        "/Subject": "Flight reservation system with admin approval workflow",
        "/Author": "Airoplane Ticket Service Project",
        "/Keywords": "Java, Swing, flight booking, admin approval, e-ticket, PWA",
    })

    with open(dst, "wb") as handle:
        writer.write(handle)

    print(f"Stamped {total} pages -> {dst}")


if __name__ == "__main__":
    main()
