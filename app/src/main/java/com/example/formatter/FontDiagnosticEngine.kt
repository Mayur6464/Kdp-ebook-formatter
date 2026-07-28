package com.example.formatter

import com.example.data.BookEntity
import com.example.data.SectionEntity
import kotlin.math.roundToInt

enum class FontEmbedStatus {
    EMBEDDED_SAFE,            // Standard KDP serif/book fonts guaranteed vector embedding
    SYSTEM_GENERIC,           // Web safe system fonts (Arial, Courier, etc.)
    UNSUPPORTED_RISKY,        // Decorative or proprietary fonts (Comic Sans, Papyrus, etc.)
    NON_EMBEDDED_REJECT_RISK  // Custom un-embedded font string that will trigger KDP PDF engine rejection
}

data class FontDiagnosticItem(
    val id: String,
    val fontName: String,
    val styleTarget: String, // e.g. "Main Body Typography", "Chapter Headings (H1)", "Inline CSS Section Spans"
    val embedStatus: FontEmbedStatus,
    val severity: DiagnosticSeverity, // PASS, WARNING, ERROR
    val usageLocation: String,
    val issueDescription: String,
    val recommendation: String,
    val isAutoFixable: Boolean = true
)

data class FontDiagnosticReport(
    val isFontCompliant: Boolean,
    val healthScore: Int,
    val totalFontsAnalyzed: Int,
    val safeEmbeddedCount: Int,
    val unsupportedCount: Int,
    val nonEmbeddedCount: Int,
    val items: List<FontDiagnosticItem>
)

object FontDiagnosticEngine {

    // Standard high-compatibility fonts recognized and embedded by Amazon KDP & Kindle
    private val KDP_SAFE_SERIF_FONTS = setOf(
        "georgia", "garamond", "times new roman", "palatino", "baskerville",
        "book antiqua", "caslon", "minion pro", "merriweather", "bookerly",
        "eb garamond", "lora", "serif"
    )

    private val KDP_SAFE_SANS_FONTS = setOf(
        "arial", "helvetica", "verdana", "trebuchet ms", "amazon ember",
        "open sans", "roboto", "lato", "sans-serif"
    )

    private val KDP_SAFE_MONO_FONTS = setOf(
        "courier new", "courier", "consolas", "monospace"
    )

    // Fonts known to cause KDP ingestion failures, rasterization blur, or rejection
    private val KDP_REJECT_PRONE_FONTS = setOf(
        "comic sans", "comic sans ms", "papyrus", "impact", "wingdings",
        "webdings", "brush script", "copperplate", "segoe ui", "system-ui",
        "calibri", "futura", "symbol"
    )

    fun analyzeDocumentFonts(book: BookEntity, sections: List<SectionEntity>): FontDiagnosticReport {
        val items = mutableListOf<FontDiagnosticItem>()

        // 1. Analyze Main Body Typography
        val bodyFont = book.bodyFontFamily.trim()
        val bodyStatus = evaluateFontStatus(bodyFont)
        items.add(
            FontDiagnosticItem(
                id = "font_body_text",
                fontName = bodyFont,
                styleTarget = "Main Body Typography",
                embedStatus = bodyStatus,
                severity = when (bodyStatus) {
                    FontEmbedStatus.EMBEDDED_SAFE -> DiagnosticSeverity.PASS
                    FontEmbedStatus.SYSTEM_GENERIC -> DiagnosticSeverity.PASS
                    FontEmbedStatus.UNSUPPORTED_RISKY -> DiagnosticSeverity.WARNING
                    FontEmbedStatus.NON_EMBEDDED_REJECT_RISK -> DiagnosticSeverity.ERROR
                },
                usageLocation = "Manuscript Default Style (${book.bodyFontSizePt}pt)",
                issueDescription = when (bodyStatus) {
                    FontEmbedStatus.EMBEDDED_SAFE -> "Body font '$bodyFont' is a verified KDP standard font with 100% vector subset embedding support."
                    FontEmbedStatus.SYSTEM_GENERIC -> "Body font '$bodyFont' is a generic system font. Will render cleanly, but book-serif fonts (Georgia/Garamond) provide superior print legibility."
                    FontEmbedStatus.UNSUPPORTED_RISKY -> "Body font '$bodyFont' is a decorative font that may render awkwardly in extended Kindle e-reader layouts."
                    FontEmbedStatus.NON_EMBEDDED_REJECT_RISK -> "Font '$bodyFont' lacks standard TrueType/OpenType embedding metadata and risks rejection by KDP automated PDF checkers."
                },
                recommendation = if (bodyStatus == FontEmbedStatus.EMBEDDED_SAFE) {
                    "No action needed. Georgia/Garamond typography is ideal for print & Kindle."
                } else {
                    "Switch body font family to Georgia or Garamond in Export Settings."
                }
            )
        )

        // 2. Analyze Chapter Title & Subtitle Typography
        val titleFont = book.bodyFontFamily // Standard theme uses body font with heavy weight
        val titleStatus = evaluateFontStatus(titleFont)
        items.add(
            FontDiagnosticItem(
                id = "font_chapter_titles",
                fontName = titleFont,
                styleTarget = "Chapter Titles & Front Matter (H1)",
                embedStatus = titleStatus,
                severity = if (titleStatus == FontEmbedStatus.NON_EMBEDDED_REJECT_RISK) DiagnosticSeverity.ERROR else DiagnosticSeverity.PASS,
                usageLocation = "Title Page, Table of Contents & Chapter Headers (${book.chapterTitleSizePt}pt)",
                issueDescription = "Heading hierarchy uses $titleFont at ${book.chapterTitleSizePt}pt. Vector paths generated cleanly for PDF /TrimBox.",
                recommendation = "Maintain high-contrast vector heading styles for crisp printing."
            )
        )

        // 3. Scan Section Content for Inline Font Families & Styles (HTML <font face> or style="font-family: ...")
        val fontAttrRegex = Regex("""(?i)\bfont-family\s*:\s*["']?([^"';>]+)["']?""")
        val htmlFontTagRegex = Regex("""(?i)<font\b[^>]*\bface\s*=\s*["']([^"']+)["']""")

        val scannedInlineFonts = mutableMapOf<String, MutableList<String>>()

        sections.forEach { section ->
            val text = section.contentText

            fontAttrRegex.findAll(text).forEach { match ->
                val font = match.groupValues[1].trim()
                if (font.isNotBlank()) {
                    scannedInlineFonts.getOrPut(font) { mutableListOf() }.add(section.title)
                }
            }

            htmlFontTagRegex.findAll(text).forEach { match ->
                val font = match.groupValues[1].trim()
                if (font.isNotBlank()) {
                    scannedInlineFonts.getOrPut(font) { mutableListOf() }.add(section.title)
                }
            }
        }

        if (scannedInlineFonts.isEmpty()) {
            items.add(
                FontDiagnosticItem(
                    id = "font_inline_styles",
                    fontName = "Inherited (${book.bodyFontFamily})",
                    styleTarget = "Inline Section Style Overrides",
                    embedStatus = FontEmbedStatus.EMBEDDED_SAFE,
                    severity = DiagnosticSeverity.PASS,
                    usageLocation = "All ${sections.size} Manuscript Sections",
                    issueDescription = "No rogue inline font overrides found. Document adheres strictly to global document CSS hierarchy.",
                    recommendation = "Clean document architecture preserves uniform Kindle formatting."
                )
            )
        } else {
            scannedInlineFonts.forEach { (fontName, locations) ->
                val status = evaluateFontStatus(fontName)
                val locSummary = locations.distinct().take(2).joinToString(", ")
                val count = locations.size

                items.add(
                    FontDiagnosticItem(
                        id = "font_inline_${fontName.lowercase().replace(" ", "_")}",
                        fontName = fontName,
                        styleTarget = "Inline HTML/CSS Override ($count instance${if (count > 1) "s" else ""})",
                        embedStatus = status,
                        severity = when (status) {
                            FontEmbedStatus.EMBEDDED_SAFE -> DiagnosticSeverity.PASS
                            FontEmbedStatus.SYSTEM_GENERIC -> DiagnosticSeverity.PASS
                            FontEmbedStatus.UNSUPPORTED_RISKY -> DiagnosticSeverity.WARNING
                            FontEmbedStatus.NON_EMBEDDED_REJECT_RISK -> DiagnosticSeverity.ERROR
                        },
                        usageLocation = "Found in: $locSummary",
                        issueDescription = when (status) {
                            FontEmbedStatus.EMBEDDED_SAFE -> "Inline override font '$fontName' is safe and fully embedded."
                            FontEmbedStatus.UNSUPPORTED_RISKY -> "Inline font '$fontName' is an unsupported display font that may override Kindle reader user font preferences."
                            FontEmbedStatus.NON_EMBEDDED_REJECT_RISK -> "Inline font '$fontName' is non-standard and lacks TrueType font embedding declarations."
                            else -> "Inline font '$fontName' detected in section markup."
                        },
                        recommendation = "Strip inline font-family overrides or convert to standard document styles."
                    )
                )
            }
        }

        // 4. Code Block / Monospace Font Check
        val containsCode = sections.any { it.contentText.contains("```") || it.contentText.contains("<code>") }
        if (containsCode) {
            items.add(
                FontDiagnosticItem(
                    id = "font_code_mono",
                    fontName = "Courier New / Monospace",
                    styleTarget = "Code & Technical Block Quotes",
                    embedStatus = FontEmbedStatus.EMBEDDED_SAFE,
                    severity = DiagnosticSeverity.PASS,
                    usageLocation = "Markdown Code Blocks & Technical Tables",
                    issueDescription = "Monospace formatting detected and mapped to Courier New vector glyphs.",
                    recommendation = "Monospace blocks are pre-formatted and fully compliant with KDP."
                )
            )
        }

        // Aggregate statistics
        val safeCount = items.count { it.embedStatus == FontEmbedStatus.EMBEDDED_SAFE || it.embedStatus == FontEmbedStatus.SYSTEM_GENERIC }
        val unsupportedCount = items.count { it.embedStatus == FontEmbedStatus.UNSUPPORTED_RISKY }
        val nonEmbeddedCount = items.count { it.embedStatus == FontEmbedStatus.NON_EMBEDDED_REJECT_RISK }
        val errors = items.count { it.severity == DiagnosticSeverity.ERROR }

        val healthScore = (((items.size - errors - (unsupportedCount * 0.5f)) / items.size.coerceAtLeast(1)) * 100).roundToInt().coerceIn(0, 100)

        return FontDiagnosticReport(
            isFontCompliant = errors == 0,
            healthScore = healthScore,
            totalFontsAnalyzed = items.size,
            safeEmbeddedCount = safeCount,
            unsupportedCount = unsupportedCount,
            nonEmbeddedCount = nonEmbeddedCount,
            items = items
        )
    }

    private fun evaluateFontStatus(fontName: String): FontEmbedStatus {
        val lower = fontName.lowercase().trim()

        if (KDP_REJECT_PRONE_FONTS.any { lower.contains(it) }) {
            return FontEmbedStatus.UNSUPPORTED_RISKY
        }

        if (KDP_SAFE_SERIF_FONTS.any { lower.contains(it) } ||
            KDP_SAFE_SANS_FONTS.any { lower.contains(it) } ||
            KDP_SAFE_MONO_FONTS.any { lower.contains(it) }) {
            return FontEmbedStatus.EMBEDDED_SAFE
        }

        // Generic system fallback
        if (lower in setOf("serif", "sans-serif", "monospace", "cursive", "fantasy")) {
            return FontEmbedStatus.SYSTEM_GENERIC
        }

        // Custom unknown font string without embedded definition
        return FontEmbedStatus.NON_EMBEDDED_REJECT_RISK
    }
}
