package com.example.formatter

import com.example.data.BookEntity
import com.example.data.SectionEntity
import com.example.data.SectionType

object KdpDocxExporter {

    /**
     * Generates a KDP-Ready HTML / Word OpenXML styled document text string
     * with exact 6"x9" page definitions, 1" margins, Georgia 12pt body font,
     * Heading 1 (20pt) and Heading 2 (15pt), running headers, page breaks,
     * and dynamic Table of Contents.
     */
    fun generateKdpHtmlFormattedDocument(book: BookEntity, sections: List<SectionEntity>): String {
        val sb = StringBuilder()

        sb.append("""
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8"/>
<title>${book.title} - KDP Print Ready</title>
<style>
@page {
    size: ${book.trimWidthInches}in ${book.trimHeightInches}in;
    margin-top: ${book.marginTopInches}in;
    margin-bottom: ${book.marginBottomInches}in;
    margin-left: ${book.marginLeftInches + book.gutterInches}in;
    margin-right: ${book.marginRightInches}in;
    @top-center {
        content: "${book.title}";
        font-family: Georgia, serif;
        font-size: 9pt;
        color: #555555;
    }
    @bottom-center {
        content: counter(page);
        font-family: Georgia, serif;
        font-size: 10pt;
    }
}

body {
    font-family: "${book.bodyFontFamily}", Georgia, "Times New Roman", serif;
    font-size: ${book.bodyFontSizePt}pt;
    line-height: ${book.lineSpacingMultiplier};
    color: #111111;
    text-align: justify;
    orphans: 2;
    widows: 2;
}

h1.chapter-title {
    font-family: "${book.bodyFontFamily}", Georgia, serif;
    font-size: ${book.chapterTitleSizePt}pt;
    font-weight: bold;
    text-align: center;
    margin-top: 2.0in;
    margin-bottom: 0.3in;
    page-break-before: always;
    text-transform: uppercase;
    letter-spacing: 1px;
}

h2.chapter-subtitle {
    font-family: "${book.bodyFontFamily}", Georgia, serif;
    font-size: ${book.heading2SizePt}pt;
    font-weight: normal;
    font-style: italic;
    text-align: center;
    margin-bottom: 0.5in;
}

h2.section-heading {
    font-family: "${book.bodyFontFamily}", Georgia, serif;
    font-size: ${book.heading2SizePt}pt;
    font-weight: bold;
    margin-top: 0.3in;
    margin-bottom: 0.15in;
    text-align: left;
}

p.epigraph {
    font-style: italic;
    text-align: center;
    margin-left: 10%;
    margin-right: 10%;
    margin-bottom: 0.4in;
    font-size: 11pt;
}

p {
    text-indent: 0.25in;
    margin-top: 0;
    margin-bottom: 0;
}

p.first-para {
    text-indent: 0;
}

.page-break {
    page-break-before: always;
}

.toc-container {
    margin-top: 0.5in;
    font-family: Georgia, serif;
}

.toc-entry {
    display: flex;
    justify-content: space-between;
    margin-bottom: 0.15in;
    font-size: 11pt;
}

.copyright-text {
    font-size: 10pt;
    line-height: 1.4;
    text-align: left;
    margin-top: 1.5in;
}
</style>
</head>
<body>
        """.trimIndent())

        // Render Front Matter, Chapters, Back Matter
        sections.forEachIndexed { index, sec ->
            if (index > 0 && sec.pageBreakBefore) {
                sb.append("<div class=\"page-break\"></div>\n")
            }

            when (sec.sectionType) {
                SectionType.TABLE_OF_CONTENTS -> {
                    sb.append("<h1 class=\"chapter-title\">Table of Contents</h1>\n")
                    sb.append("<div class=\"toc-container\">\n")
                    var pageEstimate = 1
                    sections.forEach { tocSec ->
                        if (tocSec.isIncludedInToc && tocSec.sectionType != SectionType.TABLE_OF_CONTENTS) {
                            sb.append("<div class=\"toc-entry\">")
                            sb.append("<span>${tocSec.title}</span>")
                            sb.append("<span>${pageEstimate}</span>")
                            sb.append("</div>\n")
                            pageEstimate += 3
                        }
                    }
                    sb.append("</div>\n")
                }
                SectionType.COPYRIGHT -> {
                    sb.append("<h1 class=\"chapter-title\">Copyright</h1>\n")
                    sb.append("<div class=\"copyright-text\">\n")
                    sec.contentText.split("\n\n").forEach { para ->
                        sb.append("<p class=\"first-para\">${escapeHtml(para)}</p><br/>\n")
                    }
                    sb.append("</div>\n")
                }
                else -> {
                    sb.append("<h1 class=\"chapter-title\">${escapeHtml(sec.title)}</h1>\n")
                    if (sec.subtitle.isNotBlank()) {
                        sb.append("<h2 class=\"chapter-subtitle\">${escapeHtml(sec.subtitle)}</h2>\n")
                    }
                    if (sec.epigraph.isNotBlank()) {
                        sb.append("<p class=\"epigraph\">${escapeHtml(sec.epigraph)}</p>\n")
                    }

                    val paragraphs = sec.contentText.split("\n\n")
                    paragraphs.forEachIndexed { pIdx, para ->
                        val trimmed = para.trim()
                        if (trimmed.startsWith("## ")) {
                            val headingText = trimmed.removePrefix("## ").trim()
                            sb.append("<h2 class=\"section-heading\">${escapeHtml(headingText)}</h2>\n")
                        } else {
                            val isFirst = (pIdx == 0 && sec.epigraph.isBlank())
                            val pClass = if (isFirst) "first-para" else ""
                            sb.append("<p class=\"$pClass\">${escapeHtml(trimmed)}</p>\n")
                        }
                    }
                }
            }
        }

        sb.append("\n</body>\n</html>")
        return sb.toString()
    }

    /**
     * Generates plain text formatted for direct Google Docs paste or plain text export
     */
    fun generateGoogleDocsPasteText(book: BookEntity, sections: List<SectionEntity>): String {
        val sb = StringBuilder()
        sb.append("====================================================\n")
        sb.append("${book.title.uppercase()}\n")
        sb.append("${book.subtitle}\n")
        sb.append("By ${book.author}\n")
        sb.append("Format: KDP Paperback (6\" x 9\" Trim | 1\" Margins + Gutter)\n")
        sb.append("====================================================\n\n")

        sections.forEach { sec ->
            sb.append("\n----------------------------------------------------\n")
            sb.append("[PAGE BREAK]\n")
            sb.append("${sec.title.uppercase()}\n")
            if (sec.subtitle.isNotBlank()) {
                sb.append("${sec.subtitle}\n")
            }
            if (sec.epigraph.isNotBlank()) {
                sb.append("Epigraph: ${sec.epigraph}\n")
            }
            sb.append("----------------------------------------------------\n\n")

            if (sec.sectionType == SectionType.TABLE_OF_CONTENTS) {
                sections.forEach { tocSec ->
                    if (tocSec.isIncludedInToc && tocSec.sectionType != SectionType.TABLE_OF_CONTENTS) {
                        sb.append("• ${tocSec.title}\n")
                    }
                }
            } else {
                sb.append("${sec.contentText}\n")
            }
            sb.append("\n")
        }

        return sb.toString()
    }

    private fun escapeHtml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
    }
}
