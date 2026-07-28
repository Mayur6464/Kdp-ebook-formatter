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

    /**
     * Generates an Interactive Web Edition (HTML5) document with responsive reader UI,
     * theme switching (Light, Sepia, Night), font sizing controls, interactive TOC,
     * and Kindle Web Cloud Reader formatting.
     */
    fun generateInteractiveWebBookHtml(book: BookEntity, sections: List<SectionEntity>): String {
        val sb = StringBuilder()
        sb.append("""
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
<title>${escapeHtml(book.title)} - Web Reader Edition</title>
<style>
:root {
    --bg-color: #faf7f2;
    --text-color: #2b2b2b;
    --card-bg: #ffffff;
    --border-color: #e2dcd2;
    --accent-color: #d4af37;
    --sidebar-bg: #f3edd7;
    --font-family: 'Georgia', serif;
    --font-size: 18px;
    --line-height: 1.7;
}

[data-theme="sepia"] {
    --bg-color: #f4ecd8;
    --text-color: #5b4636;
    --card-bg: #fbf0d9;
    --border-color: #e4d3b6;
    --sidebar-bg: #ebd8b7;
}

[data-theme="dark"] {
    --bg-color: #1a1a1a;
    --text-color: #e0e0e0;
    --card-bg: #242424;
    --border-color: #333333;
    --sidebar-bg: #111111;
}

* { box-sizing: border-box; margin: 0; padding: 0; }
body {
    background-color: var(--bg-color);
    color: var(--text-color);
    font-family: var(--font-family);
    font-size: var(--font-size);
    line-height: var(--line-height);
    transition: background-color 0.3s, color 0.3s;
}

header {
    background: var(--card-bg);
    border-bottom: 1px solid var(--border-color);
    padding: 12px 24px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    position: sticky;
    top: 0;
    z-index: 100;
}

.book-title-header {
    font-size: 1.1rem;
    font-weight: bold;
    color: var(--text-color);
}

.controls-bar {
    display: flex;
    gap: 12px;
    align-items: center;
}

.btn {
    background: var(--card-bg);
    border: 1px solid var(--border-color);
    color: var(--text-color);
    padding: 6px 14px;
    border-radius: 6px;
    cursor: pointer;
    font-size: 0.9rem;
    font-weight: 500;
}

.btn:hover {
    border-color: var(--accent-color);
}

.layout-container {
    display: flex;
    max-width: 1200px;
    margin: 0 auto;
    min-height: calc(100vh - 60px);
}

nav.sidebar {
    width: 280px;
    background: var(--sidebar-bg);
    border-right: 1px solid var(--border-color);
    padding: 20px;
    flex-shrink: 0;
}

.toc-title {
    font-size: 1rem;
    font-weight: bold;
    margin-bottom: 12px;
    text-transform: uppercase;
    letter-spacing: 1px;
}

.toc-list { list-style: none; }
.toc-list li { margin-bottom: 8px; }
.toc-list a {
    color: var(--text-color);
    text-decoration: none;
    font-size: 0.95rem;
    display: block;
    padding: 6px 10px;
    border-radius: 4px;
}
.toc-list a:hover {
    background: var(--card-bg);
}

main.reader-content {
    flex-grow: 1;
    padding: 40px 60px;
    max-width: 800px;
    margin: 0 auto;
}

.chapter-block {
    margin-bottom: 60px;
    padding-bottom: 40px;
    border-bottom: 1px dashed var(--border-color);
}

h1.chapter-title {
    font-size: 2.2rem;
    margin-bottom: 10px;
    text-align: center;
    color: var(--text-color);
}

h2.chapter-subtitle {
    font-size: 1.2rem;
    font-weight: normal;
    font-style: italic;
    text-align: center;
    margin-bottom: 24px;
    opacity: 0.8;
}

p {
    margin-bottom: 16px;
    text-indent: 1.5em;
    text-align: justify;
}

p.first-para { text-indent: 0; }

@media (max-width: 768px) {
    .layout-container { flex-direction: column; }
    nav.sidebar { width: 100%; border-right: none; border-bottom: 1px solid var(--border-color); }
    main.reader-content { padding: 20px; }
}
</style>
</head>
<body data-theme="light">

<header>
    <div class="book-title-header">📖 ${escapeHtml(book.title)} <span style="font-size:0.8rem; font-weight:normal; opacity:0.7;">by ${escapeHtml(book.author)}</span></div>
    <div class="controls-bar">
        <button class="btn" onclick="setTheme('light')">☀️ Light</button>
        <button class="btn" onclick="setTheme('sepia')">📜 Sepia</button>
        <button class="btn" onclick="setTheme('dark')">🌙 Dark</button>
        <button class="btn" onclick="changeFontSize(1)">A+</button>
        <button class="btn" onclick="changeFontSize(-1)">A-</button>
    </div>
</header>

<div class="layout-container">
    <nav class="sidebar">
        <div class="toc-title">Table of Contents</div>
        <ul class="toc-list">
""").append("\n")

        sections.forEachIndexed { idx, sec ->
            if (sec.isIncludedInToc) {
                sb.append("            <li><a href=\"#section-$idx\">${escapeHtml(sec.title)}</a></li>\n")
            }
        }

        sb.append("""
        </ul>
    </nav>

    <main class="reader-content">
""").append("\n")

        sections.forEachIndexed { idx, sec ->
            sb.append("        <article id=\"section-$idx\" class=\"chapter-block\">\n")
            sb.append("            <h1 class=\"chapter-title\">${escapeHtml(sec.title)}</h1>\n")
            if (sec.subtitle.isNotBlank()) {
                sb.append("            <h2 class=\"chapter-subtitle\">${escapeHtml(sec.subtitle)}</h2>\n")
            }

            val paragraphs = sec.contentText.split("\n\n")
            paragraphs.forEachIndexed { pIdx, para ->
                val trimmed = para.trim()
                if (trimmed.isNotBlank()) {
                    val pClass = if (pIdx == 0 && sec.epigraph.isBlank()) "first-para" else ""
                    sb.append("            <p class=\"$pClass\">${escapeHtml(trimmed)}</p>\n")
                }
            }
            sb.append("        </article>\n")
        }

        sb.append("""
    </main>
</div>

<script>
function setTheme(theme) {
    document.body.setAttribute('data-theme', theme);
}
let currentSize = 18;
function changeFontSize(delta) {
    currentSize = Math.max(14, Math.min(26, currentSize + delta));
    document.documentElement.style.setProperty('--font-size', currentSize + 'px');
}
</script>

</body>
</html>
""").append("\n")

        return sb.toString()
    }

    /**
     * Generates a web embed <iframe> snippet ready for author websites
     */
    fun generateWebEmbedSnippet(book: BookEntity): String {
        val safeTitle = escapeHtml(book.title)
        return """
<!-- KDP Formatter Web Reader Embed Widget for ${safeTitle} -->
<div style="max-width:100%; width:800px; height:600px; border:1px solid #e0e0e0; border-radius:12px; overflow:hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.08);">
    <iframe src="web_reader_${book.title.lowercase().replace(" ", "_")}.html" 
            title="${safeTitle} Web Reader" 
            width="100%" 
            height="100%" 
            style="border:none;">
    </iframe>
</div>
""".trimIndent()
    }

    private fun escapeHtml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
    }
}

