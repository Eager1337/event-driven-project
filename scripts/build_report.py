#!/usr/bin/env python3
"""Render the academic project report in Tahoma 10 with screenshots and optional PDF."""
from pathlib import Path
import argparse
import re
import subprocess
import tempfile
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml.ns import qn

ROOT = Path(__file__).resolve().parent.parent

def clean(text):
    return re.sub(r'\*\*(.*?)\*\*', r'\1', text).replace('  ', ' ')

def build(pdf=False):
    doc = Document()
    section = doc.sections[0]
    section.page_width, section.page_height = Inches(8.27), Inches(11.69)  # A4
    section.top_margin = section.bottom_margin = Inches(0.8)
    section.left_margin = section.right_margin = Inches(0.8)

    normal = doc.styles['Normal']
    normal.font.name = 'Tahoma'
    normal.font.size = Pt(10)
    normal.paragraph_format.line_spacing = 1.15
    normal.paragraph_format.space_after = Pt(6)
    normal.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY

    for name in ['Title', 'Heading 1', 'Heading 2', 'Heading 3']:
        if name in doc.styles:
            doc.styles[name].font.name = 'Tahoma'
            doc.styles[name].font.size = Pt(11 if name != 'Title' else 14)
            doc.styles[name].font.bold = True
            doc.styles[name].paragraph_format.line_spacing = 1.15
            doc.styles[name].paragraph_format.space_before = Pt(10)
            doc.styles[name].paragraph_format.space_after = Pt(4)

    lines = (ROOT / 'docs/report.md').read_text(encoding='utf-8').splitlines()
    buffer = []

    def flush():
        if buffer:
            p = doc.add_paragraph(clean(' '.join(buffer)))
            p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
            buffer.clear()

    for line in lines:
        stripped = line.strip()
        if not stripped or stripped == '---':
            flush()
        elif line.startswith('# '):
            flush()
            p = doc.add_heading(clean(line[2:]), 0)
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        elif line.startswith('## '):
            flush()
            doc.add_heading(clean(line[3:]), 1)
        elif line.startswith('### '):
            flush()
            doc.add_heading(clean(line[4:]), 2)
        elif line.startswith('- '):
            flush()
            p = doc.add_paragraph(clean(line[2:]), 'List Bullet')
            p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
        elif line.startswith('1. ') or line.startswith('2. ') or line.startswith('3. ') or line.startswith('4. ') or line.startswith('5. ') or line.startswith('6. '):
            flush()
            p = doc.add_paragraph(clean(line[3:]), 'List Number')
            p.alignment = WD_ALIGN_PARAGRAPH.JUSTIFY
        elif line.startswith('**'):
            flush()
            p = doc.add_paragraph(clean(line))
            p.alignment = WD_ALIGN_PARAGRAPH.LEFT
        else:
            buffer.append(stripped)
    flush()

    shots = [
        ('01-checkout.png', 'Figure 1: Inventory catalog and checkout workspace with category filters and product table.'),
        ('02-cart.png', 'Figure 2: Active shopping cart demonstrating quantity tracking and 10% promotional discount calculation.'),
        ('03-receipt.png', 'Figure 3: Persisted receipt dialog with transaction metadata, itemized breakdown, and change due.'),
        ('04-history.png', 'Figure 4: Sales transaction history audit view with tabular records of completed transactions.'),
        ('05-accessibility.png', 'Figure 5: Universal accessibility panel showing keyboard shortcut references, font scaling, and high-contrast mode.'),
        ('06-history-pagination.png', 'Figure 6: Keyset pagination view displaying the first page of sales transactions with Older Sales navigation.'),
        ('07-older-history.png', 'Figure 7: Keyset pagination older sales history view displaying historical records seamlessly.'),
        ('08-database-schema.png', 'Figure 8: MySQL relational database schema in phpMyAdmin showing products, sales, and sale_items tables.'),
        ('09-database-seed.png', 'Figure 9: Initialized product catalog and seed inventory records in phpMyAdmin.')
    ]

    doc.add_page_break()
    doc.add_heading('System Demonstration & Screenshot Appendix', 1)

    for filename, caption in shots:
        image = ROOT / 'docs/screenshots' / filename
        if image.exists():
            p_heading = doc.add_heading(caption, 2)
            p_heading.paragraph_format.space_before = Pt(12)
            p_heading.paragraph_format.space_after = Pt(4)
            doc.add_picture(str(image), width=Inches(6.5))
            p_cap = doc.add_paragraph(caption)
            p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
            for r in p_cap.runs:
                r.font.name = 'Tahoma'
                r.font.size = Pt(9)
                r.font.italic = True

    footer = section.footer.paragraphs[0]
    footer.text = 'PROG209 Event Driven Programming | Limkokwing University of Creative Technology | MarketFlow POS System'
    footer.alignment = WD_ALIGN_PARAGRAPH.CENTER
    for run in footer.runs:
        run.font.name = 'Tahoma'
        run.font.size = Pt(8)

    # Ensure Tahoma across all runs
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
            run.font.name = 'Tahoma'

    out = ROOT / 'docs/Project-Report.docx'
    doc.save(out)
    print('Report saved:', out)

    if pdf:
        with tempfile.TemporaryDirectory() as profile:
            for style in doc.styles:
                if hasattr(style, 'font'):
                    style.font.name = 'Liberation Sans'
            for paragraph in doc.paragraphs:
                for run in paragraph.runs:
                    run.font.name = 'Liberation Sans'
            for run in footer.runs:
                run.font.name = 'Liberation Sans'
            pdf_source = Path(profile) / out.name
            doc.save(pdf_source)
            subprocess.run([
                'libreoffice',
                '-env:UserInstallation=' + (Path(profile) / 'lo').as_uri(),
                '--headless',
                '--convert-to', 'pdf',
                '--outdir', str(ROOT / 'docs'),
                str(pdf_source)
            ], check=True, timeout=120)
        pdf_out = ROOT / 'docs/Project-Report.pdf'
        if not pdf_out.exists():
            raise RuntimeError('LibreOffice did not produce a PDF.')
        print('PDF saved:', pdf_out)

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--pdf', action='store_true', help='Also render a PDF using LibreOffice')
    build(parser.parse_args().pdf)
