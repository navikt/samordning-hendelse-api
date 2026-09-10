#!/usr/bin/env python3
"""Generate a styled PDF for the ROS analysis of samordning-hendelse-api feeds."""

import subprocess
import os
import sys

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
OUTPUT_PDF = os.path.join(SCRIPT_DIR, "ros-samordning-feeds.pdf")

MARKDOWN_FILES = [
    os.path.join(SCRIPT_DIR, "ros-person-feed.md"),
    os.path.join(SCRIPT_DIR, "ros-manglende-refusjon-feed.md"),
]

CSS = """
body {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
    font-size: 11pt;
    line-height: 1.5;
    color: #262626;
    max-width: 210mm;
    margin: 0 auto;
    padding: 20mm 15mm;
}
h1 {
    color: #0067C5;
    border-bottom: 3px solid #0067C5;
    padding-bottom: 8px;
    font-size: 22pt;
    page-break-before: always;
}
h1:first-child { page-break-before: avoid; }
h2 {
    color: #0067C5;
    font-size: 16pt;
    margin-top: 24px;
    border-bottom: 1px solid #CCE1F3;
    padding-bottom: 4px;
}
h3 {
    color: #333;
    font-size: 13pt;
    margin-top: 18px;
}
table {
    border-collapse: collapse;
    width: 100%;
    margin: 12px 0;
    font-size: 9.5pt;
}
th {
    background-color: #0067C5;
    color: white;
    padding: 8px 6px;
    text-align: left;
    font-weight: 600;
}
td {
    padding: 6px;
    border-bottom: 1px solid #ddd;
    vertical-align: top;
}
tr:nth-child(even) { background-color: #f8f9fa; }
code {
    background-color: #f0f4f8;
    padding: 2px 5px;
    border-radius: 3px;
    font-size: 9pt;
    font-family: 'SF Mono', Monaco, Menlo, monospace;
}
pre {
    background-color: #f0f4f8;
    padding: 12px;
    border-radius: 6px;
    font-size: 8.5pt;
    overflow-x: auto;
    border-left: 4px solid #0067C5;
}
pre code { background: none; padding: 0; }
strong { color: #1a1a1a; }
hr {
    border: none;
    border-top: 2px solid #CCE1F3;
    margin: 24px 0;
}
.cover-page {
    text-align: center;
    padding-top: 120px;
}
"""

COVER_PAGE = """
<div style="text-align: center; padding-top: 100px;">
<h1 style="border: none; color: #0067C5; font-size: 28pt; page-break-before: avoid;">
Risiko- og sårbarhetsanalyse
</h1>
<h2 style="border: none; color: #333; font-size: 18pt;">
samordning-hendelse-api
</h2>
<p style="font-size: 14pt; color: #666; margin-top: 40px;">
PersonFeedController &amp; ManglendeRefusjonFeedController
</p>
<br/><br/>
<table style="width: 50%; margin: 40px auto; font-size: 11pt;">
<tr><td style="border: none; text-align: right; color: #666; padding: 4px 12px;"><strong>Team:</strong></td>
<td style="border: none; padding: 4px 12px;">pensjonsamhandling</td></tr>
<tr><td style="border: none; text-align: right; color: #666; padding: 4px 12px;"><strong>Dato:</strong></td>
<td style="border: none; padding: 4px 12px;">2026-05-27</td></tr>
<tr><td style="border: none; text-align: right; color: #666; padding: 4px 12px;"><strong>Versjon:</strong></td>
<td style="border: none; padding: 4px 12px;">1.0</td></tr>
<tr><td style="border: none; text-align: right; color: #666; padding: 4px 12px;"><strong>Klassifisering:</strong></td>
<td style="border: none; padding: 4px 12px;">Intern</td></tr>
</table>
</div>
"""


def main():
    css_file = os.path.join(SCRIPT_DIR, ".ros-style.css")
    with open(css_file, "w") as f:
        f.write(CSS)

    for md_file in MARKDOWN_FILES:
        if not os.path.exists(md_file):
            print(f"ERROR: {md_file} not found")
            sys.exit(1)

    # Convert each markdown to HTML fragment
    html_parts = [COVER_PAGE]
    for md_file in MARKDOWN_FILES:
        result = subprocess.run(
            ["pandoc", "-f", "markdown", "-t", "html5", "--wrap=none", md_file],
            capture_output=True, text=True
        )
        if result.returncode != 0:
            print(f"ERROR pandoc: {result.stderr}")
            sys.exit(1)
        html_parts.append(result.stdout)

    # Combine into full HTML
    full_html = f"""<!DOCTYPE html>
<html lang="no">
<head>
<meta charset="utf-8"/>
<style>{CSS}</style>
</head>
<body>
{'<div style="page-break-after: always;"></div>'.join(html_parts)}
</body>
</html>"""

    html_file = os.path.join(SCRIPT_DIR, ".ros-combined.html")
    with open(html_file, "w") as f:
        f.write(full_html)

    # Try multiple PDF conversion methods
    pdf_generated = False

    # Method 1: pandoc with built-in HTML engine
    try:
        result = subprocess.run(
            ["pandoc", html_file, "-f", "html", "-t", pdf_engine(),
             "-o", OUTPUT_PDF,
             "--pdf-engine=wkhtmltopdf"],
            capture_output=True, text=True, timeout=30
        )
        if result.returncode == 0:
            pdf_generated = True
    except (subprocess.TimeoutExpired, FileNotFoundError):
        pass

    # Method 2: Use Python reportlab
    if not pdf_generated:
        print("Pandoc PDF failed, using reportlab...")
        pdf_generated = generate_pdf_reportlab(html_file)

    # Clean up temp files
    for f in [css_file, html_file]:
        if os.path.exists(f):
            os.remove(f)

    if pdf_generated:
        print(f"PDF generated: {OUTPUT_PDF}")
    else:
        # Fallback: save HTML for manual conversion
        fallback = os.path.join(SCRIPT_DIR, "ros-samordning-feeds.html")
        with open(fallback, "w") as f:
            f.write(full_html)
        print(f"PDF generation failed. HTML saved: {fallback}")
        print("Open the HTML file in a browser and print to PDF.")


def pdf_engine():
    """Detect available PDF engine for pandoc."""
    for engine in ["xelatex", "pdflatex", "wkhtmltopdf", "weasyprint"]:
        try:
            subprocess.run(["which", engine], capture_output=True, check=True)
            return "pdf"
        except subprocess.CalledProcessError:
            continue
    return "html"


def generate_pdf_reportlab(html_file):
    """Generate PDF using reportlab."""
    try:
        from reportlab.lib.pagesizes import A4
        from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
        from reportlab.lib.units import mm
        from reportlab.lib.colors import HexColor
        from reportlab.platypus import (
            SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle,
            PageBreak, HRFlowable
        )
        from reportlab.lib.enums import TA_CENTER, TA_LEFT

        doc = SimpleDocTemplate(
            OUTPUT_PDF, pagesize=A4,
            leftMargin=20*mm, rightMargin=20*mm,
            topMargin=20*mm, bottomMargin=20*mm
        )

        styles = getSampleStyleSheet()
        nav_blue = HexColor("#0067C5")
        nav_light = HexColor("#CCE1F3")

        styles.add(ParagraphStyle(
            'CoverTitle', parent=styles['Title'],
            fontSize=26, textColor=nav_blue, spaceAfter=12, alignment=TA_CENTER
        ))
        styles.add(ParagraphStyle(
            'CoverSubtitle', parent=styles['Title'],
            fontSize=16, textColor=HexColor("#333"), spaceAfter=8, alignment=TA_CENTER
        ))
        styles.add(ParagraphStyle(
            'CoverMeta', parent=styles['Normal'],
            fontSize=12, textColor=HexColor("#666"), alignment=TA_CENTER, spaceAfter=4
        ))
        styles.add(ParagraphStyle(
            'H1', parent=styles['Heading1'],
            fontSize=20, textColor=nav_blue, spaceBefore=24, spaceAfter=10,
            borderWidth=2, borderColor=nav_blue, borderPadding=4
        ))
        styles.add(ParagraphStyle(
            'H2', parent=styles['Heading2'],
            fontSize=14, textColor=nav_blue, spaceBefore=16, spaceAfter=8
        ))
        styles.add(ParagraphStyle(
            'H3', parent=styles['Heading3'],
            fontSize=12, textColor=HexColor("#333"), spaceBefore=12, spaceAfter=6
        ))
        styles.add(ParagraphStyle(
            'BodyText2', parent=styles['Normal'],
            fontSize=10, leading=14, spaceAfter=6
        ))
        styles.add(ParagraphStyle(
            'CellText', parent=styles['Normal'],
            fontSize=8, leading=10
        ))
        styles.add(ParagraphStyle(
            'CellHeader', parent=styles['Normal'],
            fontSize=8, leading=10, textColor=HexColor("#FFFFFF"), fontName='Helvetica-Bold'
        ))

        story = []

        # Cover page
        story.append(Spacer(1, 80*mm))
        story.append(Paragraph("Risiko- og sårbarhetsanalyse", styles['CoverTitle']))
        story.append(Spacer(1, 8*mm))
        story.append(Paragraph("samordning-hendelse-api", styles['CoverSubtitle']))
        story.append(Spacer(1, 12*mm))
        story.append(Paragraph("PersonFeedController &amp; ManglendeRefusjonFeedController", styles['CoverMeta']))
        story.append(Spacer(1, 20*mm))
        story.append(Paragraph("Team: pensjonsamhandling", styles['CoverMeta']))
        story.append(Paragraph("Dato: 2026-05-27", styles['CoverMeta']))
        story.append(Paragraph("Versjon: 1.0", styles['CoverMeta']))
        story.append(Paragraph("Klassifisering: Intern", styles['CoverMeta']))
        story.append(PageBreak())

        # Parse and render each markdown file
        for md_file in MARKDOWN_FILES:
            with open(md_file, 'r') as f:
                lines = f.readlines()

            i = 0
            table_rows = []
            in_table = False

            while i < len(lines):
                line = lines[i].rstrip()

                # Skip empty lines
                if not line:
                    if in_table and table_rows:
                        story.append(build_table(table_rows, styles))
                        table_rows = []
                        in_table = False
                    i += 1
                    continue

                # Headers
                if line.startswith('# ') and not line.startswith('## '):
                    if in_table and table_rows:
                        story.append(build_table(table_rows, styles))
                        table_rows = []
                        in_table = False
                    story.append(Paragraph(clean_md(line[2:]), styles['H1']))
                    i += 1
                    continue
                if line.startswith('## '):
                    if in_table and table_rows:
                        story.append(build_table(table_rows, styles))
                        table_rows = []
                        in_table = False
                    story.append(Paragraph(clean_md(line[3:]), styles['H2']))
                    i += 1
                    continue
                if line.startswith('### '):
                    if in_table and table_rows:
                        story.append(build_table(table_rows, styles))
                        table_rows = []
                        in_table = False
                    story.append(Paragraph(clean_md(line[4:]), styles['H3']))
                    i += 1
                    continue

                # Horizontal rule
                if line.startswith('---'):
                    if in_table and table_rows:
                        story.append(build_table(table_rows, styles))
                        table_rows = []
                        in_table = False
                    story.append(HRFlowable(width="100%", color=nav_light, thickness=2, spaceAfter=8, spaceBefore=8))
                    i += 1
                    continue

                # Table
                if '|' in line and line.strip().startswith('|'):
                    cells = [c.strip() for c in line.strip().strip('|').split('|')]
                    # Skip separator rows
                    if all(c.replace('-', '').replace(':', '') == '' for c in cells):
                        i += 1
                        continue
                    table_rows.append(cells)
                    in_table = True
                    i += 1
                    continue

                # Code block
                if line.startswith('```'):
                    if in_table and table_rows:
                        story.append(build_table(table_rows, styles))
                        table_rows = []
                        in_table = False
                    code_lines = []
                    i += 1
                    while i < len(lines) and not lines[i].strip().startswith('```'):
                        code_lines.append(lines[i].rstrip())
                        i += 1
                    code_text = '<br/>'.join(
                        l.replace(' ', '&nbsp;').replace('<', '&lt;').replace('>', '&gt;')
                        for l in code_lines
                    )
                    story.append(Paragraph(
                        f'<font face="Courier" size="7">{code_text}</font>',
                        ParagraphStyle('Code', parent=styles['Normal'],
                                      backColor=HexColor("#f0f4f8"),
                                      borderWidth=0, leftIndent=8,
                                      spaceBefore=6, spaceAfter=6,
                                      fontSize=7, leading=9)
                    ))
                    i += 1
                    continue

                # Regular paragraph
                if in_table and table_rows:
                    story.append(build_table(table_rows, styles))
                    table_rows = []
                    in_table = False
                story.append(Paragraph(clean_md(line), styles['BodyText2']))
                i += 1

            # Flush remaining table
            if in_table and table_rows:
                story.append(build_table(table_rows, styles))

            story.append(PageBreak())

        doc.build(story)
        return True

    except Exception as e:
        print(f"Reportlab error: {e}")
        import traceback
        traceback.print_exc()
        return False


def build_table(rows, styles):
    """Build a reportlab Table from parsed markdown table rows."""
    from reportlab.platypus import Table, TableStyle, Paragraph
    from reportlab.lib.colors import HexColor
    from reportlab.lib.units import mm

    nav_blue = HexColor("#0067C5")
    cell_style = styles['CellText']
    header_style = styles['CellHeader']

    # Convert to Paragraphs for wrapping
    data = []
    for ri, row in enumerate(rows):
        style = header_style if ri == 0 else cell_style
        data.append([Paragraph(clean_md(cell), style) for cell in row])

    num_cols = max(len(r) for r in data)
    col_width = (170*mm) / num_cols

    t = Table(data, colWidths=[col_width]*num_cols, repeatRows=1)
    table_style = [
        ('BACKGROUND', (0, 0), (-1, 0), nav_blue),
        ('TEXTCOLOR', (0, 0), (-1, 0), HexColor("#FFFFFF")),
        ('FONTSIZE', (0, 0), (-1, -1), 8),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 4),
        ('TOPPADDING', (0, 0), (-1, -1), 4),
        ('LEFTPADDING', (0, 0), (-1, -1), 4),
        ('RIGHTPADDING', (0, 0), (-1, -1), 4),
        ('GRID', (0, 0), (-1, -1), 0.5, HexColor("#ddd")),
        ('VALIGN', (0, 0), (-1, -1), 'TOP'),
    ]
    # Alternating row colors
    for ri in range(1, len(data)):
        if ri % 2 == 0:
            table_style.append(('BACKGROUND', (0, ri), (-1, ri), HexColor("#f8f9fa")))

    t.setStyle(TableStyle(table_style))
    return t


def clean_md(text):
    """Clean markdown formatting for reportlab Paragraph."""
    import re
    text = re.sub(r'\*\*(.+?)\*\*', r'<b>\1</b>', text)
    text = re.sub(r'`(.+?)`', r'<font face="Courier" size="8">\1</font>', text)
    text = text.replace('🟢', '●').replace('🟡', '◐').replace('🔴', '○')
    text = text.replace('—', '–')
    return text


if __name__ == "__main__":
    main()
