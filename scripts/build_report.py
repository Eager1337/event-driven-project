#!/usr/bin/env python3
"""Render the editable Tahoma 10 report with screenshots; optional LibreOffice PDF."""
from pathlib import Path
import argparse
import re
import subprocess
import tempfile
from docx import Document
from docx.shared import Inches, Pt
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml.ns import qn

ROOT = Path(__file__).resolve().parent.parent

def clean(text):
    return re.sub(r'\*\*(.*?)\*\*', r'\1', text).replace('  ', ' ')

def build(pdf=False):
    doc = Document()
    section = doc.sections[0]
    section.page_width, section.page_height = Inches(8.27), Inches(11.69)
    section.top_margin = section.bottom_margin = Inches(0.7)
    section.left_margin = section.right_margin = Inches(0.8)
    normal = doc.styles['Normal']
    normal.font.name = 'Tahoma'
    normal.font.size = Pt(10)
    normal.paragraph_format.line_spacing = 1.15
    normal.paragraph_format.space_after = Pt(6)
    normal.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
    for name in ['Title', 'Heading 1', 'Heading 2']:
        doc.styles[name].font.name = 'Tahoma'
        doc.styles[name].font.size = Pt(10)
        doc.styles[name].font.bold = True
        doc.styles[name].paragraph_format.line_spacing = 1.15
    lines = (ROOT/'docs/report.md').read_text(encoding='utf-8').splitlines()
    buffer = []
    def flush():
        if buffer:
            doc.add_paragraph(clean(' '.join(buffer)))
            buffer.clear()
    for line in lines:
        if not line.strip():
            flush()
        elif line.startswith('# '):
            flush(); doc.add_heading(clean(line[2:]), 0)
        elif line.startswith('## '):
            flush(); doc.add_heading(clean(line[3:]), 1)
        elif line.startswith('- '):
            flush(); doc.add_paragraph(clean(line[2:]), 'List Bullet')
        elif line.startswith('**'):
            flush(); doc.add_paragraph(clean(line))
        else:
            buffer.append(line.strip())
    flush()
    shots = [('01-checkout.png', 'Inventory and checkout workspace'),
             ('02-cart.png', 'Populated cart with 10 percent discount'),
             ('03-receipt.png', 'Persisted receipt and calculated change'),
             ('04-history.png', 'Sales history from the test database'),
             ('05-accessibility.png', 'Help and accessibility options'),
             ('06-history-pagination.png', 'First page of 55 synthetic sale headers'),
             ('07-older-history.png', 'Older page with the remaining five synthetic headers')]
    for filename, caption in shots:
        image = ROOT/'docs/screenshots'/filename
        if image.exists():
            doc.add_page_break()
            doc.add_heading('Screenshot appendix — '+caption, 1)
            doc.add_paragraph('Actual Java Swing interface captured during automated tests. Synthetic data only.')
            doc.add_picture(str(image), width=Inches(6.5))
            doc.add_paragraph(caption)
    footer = section.footer.paragraphs[0]
    footer.text = 'MarketFlow | PROG209 | Java Swing & MySQL | Academic demonstration'
    footer.alignment = WD_ALIGN_PARAGRAPH.CENTER
    for run in footer.runs:
        run.font.name = 'Tahoma'; run.font.size = Pt(8)
    for style in doc.styles:
        if hasattr(style, 'font'):
            style.font.name = 'Tahoma'
            fonts = style.element.find('.//' + qn('w:rFonts'))
            if fonts is not None:
                for key in list(fonts.attrib):
                    if 'theme' in key.lower():
                        del fonts.attrib[key]
    for paragraph in doc.paragraphs:
        for run in paragraph.runs:
            run.font.name = 'Tahoma'; run.font.size = Pt(10)
    out = ROOT/'docs/Project-Report.docx'
    doc.save(out)
    if pdf:
        with tempfile.TemporaryDirectory() as profile:
            # Microsoft Tahoma is not redistributed. Render using an explicit sans-serif
            # fallback, keeping the editable DOCX's required Tahoma formatting intact.
            for style in doc.styles:
                if hasattr(style, 'font'):
                    style.font.name = 'Liberation Sans'
            for paragraph in doc.paragraphs:
                for run in paragraph.runs:
                    run.font.name = 'Liberation Sans'
            for run in footer.runs:
                run.font.name = 'Liberation Sans'
            pdf_source = Path(profile)/out.name
            doc.save(pdf_source)
            subprocess.run(['libreoffice', '-env:UserInstallation='+(Path(profile)/'lo').as_uri(), '--headless', '--convert-to', 'pdf', '--outdir', str(ROOT/'docs'), str(pdf_source)], check=True, timeout=120)
        if not (ROOT/'docs/Project-Report.pdf').exists():
            raise RuntimeError('LibreOffice did not produce a PDF.')
    print('Report created:', out)

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--pdf', action='store_true', help='Also render a PDF using installed LibreOffice')
    build(parser.parse_args().pdf)