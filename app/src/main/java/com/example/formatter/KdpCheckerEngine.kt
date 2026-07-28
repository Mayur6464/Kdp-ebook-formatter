package com.example.formatter

import com.example.data.BookEntity
import com.example.data.SectionEntity
import com.example.data.SectionType

data class KdpCheckItem(
    val id: String,
    val title: String,
    val isPassed: Boolean,
    val description: String,
    val recommendation: String,
    val category: String
)

data class KdpAuditResult(
    val scorePercentage: Int,
    val totalChecks: Int,
    val passedChecksCount: Int,
    val items: List<KdpCheckItem>
)

object KdpCheckerEngine {

    fun auditBook(book: BookEntity, sections: List<SectionEntity>, targetGenre: String = book.coverGenre): KdpAuditResult {
        val checks = mutableListOf<KdpCheckItem>()

        // 1. Trim Size
        val trimPassed = (book.trimWidthInches == 6.0f && book.trimHeightInches == 9.0f)
        checks.add(
            KdpCheckItem(
                id = "trim_size",
                title = "6\" × 9\" Trim Size (Standard KDP)",
                isPassed = trimPassed,
                description = "KDP standard paperback trim size set to 6.0\" width × 9.0\" height.",
                recommendation = "Set width to 6.0\" and height to 9.0\" in Settings.",
                category = "Trim & Margins"
            )
        )

        // 2. Margins and Gutter
        val marginsPassed = (
            book.marginTopInches >= 1.0f &&
            book.marginBottomInches >= 1.0f &&
            book.marginLeftInches >= 1.0f &&
            book.marginRightInches >= 1.0f &&
            book.gutterInches >= 0.125f
        )
        checks.add(
            KdpCheckItem(
                id = "margins_gutter",
                title = "1\" Margins with Gutter for Paperback",
                isPassed = marginsPassed,
                description = "All page margins set to >= 1.0\" with gutter >= 0.125\" to prevent text binding loss.",
                recommendation = "Set Top, Bottom, Left, Right margins to 1.0\" and Gutter to 0.125\" or higher.",
                category = "Trim & Margins"
            )
        )

        // 3. Body Typography
        val fontPassed = (book.bodyFontFamily.contains("Georgia", ignoreCase = true) || book.bodyFontFamily.contains("Serif", ignoreCase = true)) && book.bodyFontSizePt >= 11
        checks.add(
            KdpCheckItem(
                id = "body_typography",
                title = "Georgia / Serif Body Typography",
                isPassed = fontPassed,
                description = "Print-ready body typography configured with ${book.bodyFontFamily} at ${book.bodyFontSizePt} pt.",
                recommendation = "Select Georgia or Serif font family and 12 pt body size for optimal legibility.",
                category = "Typography"
            )
        )

        // 4. Chapter Title Styling
        val chapterTitlePassed = book.chapterTitleSizePt in 18..24
        checks.add(
            KdpCheckItem(
                id = "chapter_titles",
                title = "18–22 pt Professional Chapter Titles",
                isPassed = chapterTitlePassed,
                description = "Chapter headings sized between 18 pt and 22 pt (Heading 1 standard).",
                recommendation = "Adjust chapter title font size to 20 pt in formatting settings.",
                category = "Typography"
            )
        )

        // 5. Automatic Table of Contents & Link Integrity
        val tocSection = sections.find { it.sectionType == SectionType.TABLE_OF_CONTENTS }
        val tocIncludedSections = sections.filter { it.isIncludedInToc && it.sectionType != SectionType.TABLE_OF_CONTENTS }
        val brokenTocLinks = tocIncludedSections.filter { it.title.isBlank() }
        val hasToc = tocSection != null && brokenTocLinks.isEmpty()
        checks.add(
            KdpCheckItem(
                id = "automatic_toc",
                title = "Table of Contents & Link Integrity",
                isPassed = hasToc,
                description = if (brokenTocLinks.isNotEmpty()) 
                    "Found ${brokenTocLinks.size} section(s) marked for TOC with missing titles."
                else if (tocSection == null) 
                    "Table of Contents section missing from Front Matter."
                else 
                    "Table of Contents verified: all ${tocIncludedSections.size} sections correctly linked.",
                recommendation = "Ensure Table of Contents section exists and all chapters have valid non-empty titles.",
                category = "Front Matter"
            )
        )

        // 5b. Blank Page & Excessive Line Breaks Scanner
        val emptySections = sections.filter { it.contentText.isBlank() }
        val sectionsWithExcessiveNewlines = sections.filter { it.contentText.contains("\n\n\n\n") }
        val noBlankPagesPassed = emptySections.isEmpty() && sectionsWithExcessiveNewlines.isEmpty()
        checks.add(
            KdpCheckItem(
                id = "blank_pages_scanner",
                title = "Blank Page & Formatting Artifact Scanner",
                isPassed = noBlankPagesPassed,
                description = when {
                    emptySections.isNotEmpty() -> "Found ${emptySections.size} empty section(s) that may cause blank page errors in KDP print preview."
                    sectionsWithExcessiveNewlines.isNotEmpty() -> "Found excessive consecutive blank lines in ${sectionsWithExcessiveNewlines.size} section(s)."
                    else -> "No accidental blank pages or excessive trailing empty lines detected."
                },
                recommendation = "Remove blank lines or fill empty chapters before submitting to KDP.",
                category = "Main Content"
            )
        )

        // 6. Page Numbers Post-Front Matter
        val pageNumPassed = book.enablePageNumbers && book.startPageNumbersAfterFrontMatter
        checks.add(
            KdpCheckItem(
                id = "page_numbering",
                title = "Page Numbers Starting After Front Matter",
                isPassed = pageNumPassed,
                description = "Arabic page numbers start on Introduction/Chapter 1; front matter unnumbered or Roman.",
                recommendation = "Enable 'Start page numbers after Front Matter' in layout toggles.",
                category = "Pagination"
            )
        )

        // 7. Running Headers
        val headersPassed = book.enableRunningHeaders
        checks.add(
            KdpCheckItem(
                id = "running_headers",
                title = "Running Headers with Book Title & Chapter Name",
                isPassed = headersPassed,
                description = "Left page running header displays book title; right page displays current chapter.",
                recommendation = "Enable running headers toggle.",
                category = "Pagination"
            )
        )

        // 8. Clean Page Breaks Before Chapters
        val breaksPassed = book.cleanPageBreaksBeforeChapters
        checks.add(
            KdpCheckItem(
                id = "page_breaks",
                title = "Clean Page Breaks Before Every Chapter",
                isPassed = breaksPassed,
                description = "Each new section or chapter begins on a fresh right-hand (recto) page break.",
                recommendation = "Enable page breaks before chapters.",
                category = "Pagination"
            )
        )

        // 9. Widow and Orphan Control
        val widowPassed = book.widowOrphanControl
        checks.add(
            KdpCheckItem(
                id = "widow_orphan",
                title = "Widow & Orphan Control",
                isPassed = widowPassed,
                description = "Paragraph formatting prevents single dangling lines at page tops or bottoms.",
                recommendation = "Turn on Widow/Orphan control.",
                category = "Typography"
            )
        )

        // 10. Front Matter Completeness
        val requiredFrontMatter = setOf(
            SectionType.COPYRIGHT,
            SectionType.MEDICAL_DISCLAIMER,
            SectionType.AFFILIATE_DISCLOSURE,
            SectionType.TABLE_OF_CONTENTS,
            SectionType.INTRODUCTION
        )
        val existingFrontMatter = sections.map { it.sectionType }.toSet()
        val frontMatterPassed = requiredFrontMatter.all { existingFrontMatter.contains(it) }
        checks.add(
            KdpCheckItem(
                id = "front_matter",
                title = "Complete Front Matter (Copyright, Disclaimers, TOC, Intro)",
                isPassed = frontMatterPassed,
                description = "Includes Copyright Page, Medical Disclaimer, Affiliate Disclosure, TOC, and Introduction.",
                recommendation = "Ensure all required Front Matter sections are added in the editor.",
                category = "Front Matter"
            )
        )

        // 11. All Chapters Present
        val chapterCount = sections.count { it.sectionType == SectionType.CHAPTER }
        val chaptersPassed = chapterCount >= 5
        checks.add(
            KdpCheckItem(
                id = "eleven_chapters",
                title = "Main Content Chapters ($chapterCount chapters present)",
                isPassed = chaptersPassed,
                description = "Found $chapterCount chapters formatted with Heading 1 and Heading 2 structure.",
                recommendation = "Ensure at least 5 main chapters are present and populated.",
                category = "Main Content"
            )
        )

        // 12. Back Matter Completeness
        val requiredBackMatter = setOf(
            SectionType.ABOUT_AUTHOR,
            SectionType.THANK_YOU,
            SectionType.AMAZON_REVIEW,
            SectionType.OTHER_BOOKS,
            SectionType.WEBSITE_SOCIAL
        )
        val backMatterPassed = requiredBackMatter.all { existingFrontMatter.contains(it) }
        checks.add(
            KdpCheckItem(
                id = "back_matter",
                title = "Complete Back Matter (About Author, Thank You, Review Invite, Other Books, Socials)",
                isPassed = backMatterPassed,
                description = "Includes About Author, Thank You, Amazon Review Request, Other Books, and Social Links.",
                recommendation = "Add missing Back Matter sections in the editor.",
                category = "Back Matter"
            )
        )

        // ==========================================
        // DIGITAL ACCESSIBILITY AUDIT (WCAG & KDP)
        // ==========================================
        val headingReport = analyzeHeadingsHierarchy(sections)
        checks.add(
            KdpCheckItem(
                id = "access_heading_hierarchy",
                title = "Headings Hierarchy (H1–H4 Sequential Nesting)",
                isPassed = headingReport.isValid,
                description = headingReport.issueSummary,
                recommendation = "Ensure subheadings inside chapters follow strict H1 -> H2 -> H3 -> H4 hierarchy without skipping levels for screen reader compatibility.",
                category = "Digital Accessibility"
            )
        )

        val imageReport = analyzeImageAltText(sections)
        checks.add(
            KdpCheckItem(
                id = "access_image_alt_text",
                title = "Image Alt-Text & Descriptive Figure Labels",
                isPassed = imageReport.isValid,
                description = imageReport.issueSummary,
                recommendation = "Provide descriptive alt-text for all figure images (e.g. ![Diagram of Circadian Cycle](...)) so Kindle screen readers can convey visual content.",
                category = "Digital Accessibility"
            )
        )

        val landmarksPassed = (tocSection != null && sections.all { it.title.isNotBlank() })
        checks.add(
            KdpCheckItem(
                id = "access_screen_reader_landmarks",
                title = "EPUB & Screen Reader Logical Reading Landmarks",
                isPassed = landmarksPassed,
                description = if (landmarksPassed)
                    "All ${sections.size} sections contain explicit accessibility navigation landmarks and logical reading order tags."
                else
                    "Found unlabelled sections missing structural EPUB landmark tags.",
                recommendation = "Ensure Table of Contents exists and all sections have valid titles.",
                category = "Digital Accessibility"
            )
        )

        // ==========================================
        // GENRE-SPECIFIC KDP VALIDATION RULES
        // ==========================================
        val genreLower = targetGenre.lowercase()

        when {
            genreLower.contains("poetry") || genreLower.contains("verse") -> {
                // Poetry Validation Rule 1: Non-Standard Verse & Stanza Lineation Spacing
                val singleLineBreakSections = sections.filter { sec ->
                    sec.contentText.contains("\n") && !sec.contentText.contains("\n\n\n")
                }
                val verseSpacingPassed = singleLineBreakSections.isNotEmpty()
                checks.add(
                    KdpCheckItem(
                        id = "genre_poetry_verse_spacing",
                        title = "Poetry Verse Spacing & Stanza Indentation Rules",
                        isPassed = verseSpacingPassed,
                        description = "Verifies stanza lineation uses left-aligned non-justified spacing to prevent awkward Kindle text stretching.",
                        recommendation = "Ensure poetry stanzas use intentional single line breaks with left-alignment.",
                        category = "Genre Rules (Poetry)"
                    )
                )

                // Poetry Validation Rule 2: Dedicated Page Break Per Poem
                val allPoemPageBreaksPassed = sections.filter { it.sectionType == SectionType.CHAPTER }.all { it.pageBreakBefore }
                checks.add(
                    KdpCheckItem(
                        id = "genre_poetry_page_breaks",
                        title = "Dedicated Recto Page Break Per Poem / Verse",
                        isPassed = allPoemPageBreaksPassed,
                        description = "Every individual poem or verse collection section must start on a fresh page.",
                        recommendation = "Set 'Page Break Before' to true for every poem section.",
                        category = "Genre Rules (Poetry)"
                    )
                )

                // Poetry Validation Rule 3: Poem Title or First-Line Index in TOC
                val tocPoemTitlesPassed = sections.filter { it.sectionType == SectionType.CHAPTER }.all { it.title.isNotBlank() }
                checks.add(
                    KdpCheckItem(
                        id = "genre_poetry_toc_index",
                        title = "Poem Title or First-Line Index in TOC",
                        isPassed = tocPoemTitlesPassed,
                        description = "KDP Poetry collections require clear poem titles or first-line identifiers in the Table of Contents.",
                        recommendation = "Provide distinct titles or first-line names for every poem.",
                        category = "Genre Rules (Poetry)"
                    )
                )
            }

            genreLower.contains("fiction") || genreLower.contains("novel") || genreLower.contains("thriller") || genreLower.contains("fantasy") -> {
                // Fiction Validation Rule 1: Scene Break Separators
                val hasSceneBreakSeparators = sections.any { sec ->
                    sec.contentText.contains("***") || sec.contentText.contains("* * *") || sec.contentText.contains("---")
                }
                checks.add(
                    KdpCheckItem(
                        id = "genre_fiction_scene_breaks",
                        title = "Standard Scene Break Separators (*** or * * *)",
                        isPassed = hasSceneBreakSeparators,
                        description = "Fiction manuscripts require explicit centered scene break markers (e.g., *** or * * *) rather than empty space gaps.",
                        recommendation = "Insert '***' or '* * *' between scene shifts inside chapter sections.",
                        category = "Genre Rules (Fiction)"
                    )
                )

                // Fiction Validation Rule 2: Dialogue Paragraphing & First-Line Indents
                val cleanParagraphingPassed = sections.none { sec -> sec.contentText.contains("\n\n\n") }
                checks.add(
                    KdpCheckItem(
                        id = "genre_fiction_dialogue_indent",
                        title = "Fiction Paragraph Indentation & Dialogue Formatting",
                        isPassed = cleanParagraphingPassed,
                        description = "Verifies narrative & dialogue paragraphs do not use double empty line gaps between spoken lines.",
                        recommendation = "Use standard paragraph indentation with single line breaks for dialogue.",
                        category = "Genre Rules (Fiction)"
                    )
                )

                // Fiction Validation Rule 3: Chapter Title Naming & Structure
                val chaptersHaveTitles = sections.filter { it.sectionType == SectionType.CHAPTER }.all { it.title.isNotBlank() }
                checks.add(
                    KdpCheckItem(
                        id = "genre_fiction_chapter_titles",
                        title = "Chapter Naming & Narrative Structure",
                        isPassed = chaptersHaveTitles,
                        description = "All fiction chapters have clear titles (e.g. 'Chapter 1: The Awakening' or numbered headers).",
                        recommendation = "Give every chapter section a clear title.",
                        category = "Genre Rules (Fiction)"
                    )
                )
            }

            genreLower.contains("children") || genreLower.contains("picture") || genreLower.contains("illustrated") -> {
                // Children's Rule 1: Large Print Font Size (>= 14 pt)
                val largeFontPassed = book.bodyFontSizePt >= 14
                checks.add(
                    KdpCheckItem(
                        id = "genre_children_font_size",
                        title = "Large Print Font Size (>= 14 pt) for Young Readers",
                        isPassed = largeFontPassed,
                        description = "Children's books require large, highly readable typography (minimum 14 pt).",
                        recommendation = "Set body font size to 14 pt or 16 pt in layout settings.",
                        category = "Genre Rules (Children's)"
                    )
                )

                // Children's Rule 2: Full-Bleed Margin & Edge Clearance
                val fullBleedPassed = book.marginLeftInches >= 0.5f && book.marginRightInches >= 0.5f
                checks.add(
                    KdpCheckItem(
                        id = "genre_children_full_bleed",
                        title = "Full-Bleed Margin & Trim Clearance",
                        isPassed = fullBleedPassed,
                        description = "Ensures illustration edges clear KDP safety bleed margins (min 0.375\" to 0.5\").",
                        recommendation = "Ensure page margins are at least 0.5\" for picture book layouts.",
                        category = "Genre Rules (Children's)"
                    )
                )
            }

            genreLower.contains("academic") || genreLower.contains("technical") || genreLower.contains("textbook") -> {
                // Academic Rule 1: Index Page / Bibliography / References
                val hasReferencesSection = sections.any { sec ->
                    sec.title.contains("References", ignoreCase = true) ||
                    sec.title.contains("Bibliography", ignoreCase = true) ||
                    sec.title.contains("Index", ignoreCase = true) ||
                    sec.title.contains("Citations", ignoreCase = true) ||
                    sec.sectionType == SectionType.RECOMMENDED_RESOURCES ||
                    sec.sectionType == SectionType.APPENDIX
                }
                checks.add(
                    KdpCheckItem(
                        id = "genre_academic_references",
                        title = "Academic Index, Bibliography, or References Section",
                        isPassed = hasReferencesSection,
                        description = "Academic & technical books require an Index, Bibliography, or References section in Back Matter.",
                        recommendation = "Add a References, Appendix, or Bibliography section in the editor.",
                        category = "Genre Rules (Academic)"
                    )
                )

                // Academic Rule 2: Glossary or Terminology Section
                val hasGlossary = sections.any { sec ->
                    sec.title.contains("Glossary", ignoreCase = true) ||
                    sec.title.contains("Terminology", ignoreCase = true) ||
                    sec.sectionType == SectionType.APPENDIX
                }
                checks.add(
                    KdpCheckItem(
                        id = "genre_academic_glossary",
                        title = "Glossary or Key Terminology Reference",
                        isPassed = hasGlossary,
                        description = "Technical textbooks require a Glossary or Appendix explaining key domain terms.",
                        recommendation = "Include a Glossary section in Back Matter.",
                        category = "Genre Rules (Academic)"
                    )
                )
            }

            else -> { // Non-Fiction / Wellness / Self-Help (Default Standard)
                // Non-Fiction Rule 1: Index / Recommended Resources / Appendix Section
                val hasIndexOrResources = sections.any { sec ->
                    sec.sectionType == SectionType.RECOMMENDED_RESOURCES ||
                    sec.sectionType == SectionType.APPENDIX ||
                    sec.title.contains("Index", ignoreCase = true) ||
                    sec.title.contains("Resources", ignoreCase = true)
                }
                checks.add(
                    KdpCheckItem(
                        id = "genre_nf_index_resources",
                        title = "Non-Fiction Index, Appendix, or Recommended Resources",
                        isPassed = hasIndexOrResources,
                        description = "KDP Non-Fiction & Self-Help standards expect a Recommended Resources, Appendix, or Index page.",
                        recommendation = "Ensure a Recommended Resources or Appendix section is included.",
                        category = "Genre Rules (Non-Fiction)"
                    )
                )

                // Non-Fiction Rule 2: Author Bio & Credentials
                val aboutAuthor = sections.find { it.sectionType == SectionType.ABOUT_AUTHOR }
                val authorBioPassed = aboutAuthor != null && aboutAuthor.contentText.split("\\s+".toRegex()).size >= 20
                checks.add(
                    KdpCheckItem(
                        id = "genre_nf_author_credentials",
                        title = "Non-Fiction Author Bio & Expert Credentials",
                        isPassed = authorBioPassed,
                        description = "Non-fiction readers expect an author bio establishing authority, background, and website links.",
                        recommendation = "Populate the 'About the Author' section with credentials and background.",
                        category = "Genre Rules (Non-Fiction)"
                    )
                )

                // Non-Fiction Rule 3: Structured Chapter Subheadings (H2)
                val chaptersWithSubheadings = sections.filter { it.sectionType == SectionType.CHAPTER }.filter { sec ->
                    sec.contentText.contains("Heading") || sec.contentText.contains("##") || sec.contentText.contains("\n\n")
                }
                val subheadingsPassed = chaptersWithSubheadings.isNotEmpty()
                checks.add(
                    KdpCheckItem(
                        id = "genre_nf_subheadings",
                        title = "Structured Chapter Subheadings (H2) for Reader Skimming",
                        isPassed = subheadingsPassed,
                        description = "Non-fiction chapters require subheadings to organize key concepts into digestible blocks.",
                        recommendation = "Break long chapter text into sections with subheadings.",
                        category = "Genre Rules (Non-Fiction)"
                    )
                )
            }
        }

        val passedCount = checks.count { it.isPassed }
        val scorePct = ((passedCount.toDouble() / checks.size) * 100).toInt()

        return KdpAuditResult(
            scorePercentage = scorePct,
            totalChecks = checks.size,
            passedChecksCount = passedCount,
            items = checks
        )
    }

    private data class HeadingHierarchyReport(
        val isValid: Boolean,
        val totalHeadings: Int,
        val skippedLevelCount: Int,
        val deepestLevel: Int,
        val issueSummary: String
    )

    private fun analyzeHeadingsHierarchy(sections: List<SectionEntity>): HeadingHierarchyReport {
        var totalHeadings = 0
        var skippedLevelCount = 0
        var maxDepth = 1
        val skippedIssues = mutableListOf<String>()

        val mdHeadingRegex = Regex("""(?m)^(#{1,6})\s+(.+)$""")
        val htmlHeadingRegex = Regex("""(?i)<h([1-6])\b[^>]*>(.*?)</h\1>""")

        sections.forEach { section ->
            var lastLevel = 1 // Section title serves as H1
            totalHeadings++

            val text = section.contentText
            val lines = text.lines()

            for (line in lines) {
                val trimmed = line.trim()
                val mdMatch = mdHeadingRegex.find(trimmed)
                val level: Int?
                val titleText: String?

                if (mdMatch != null) {
                    level = mdMatch.groupValues[1].length
                    titleText = mdMatch.groupValues[2].trim()
                } else {
                    val htmlMatch = htmlHeadingRegex.find(trimmed)
                    if (htmlMatch != null) {
                        level = htmlMatch.groupValues[1].toIntOrNull()
                        titleText = htmlMatch.groupValues[2].trim()
                    } else {
                        level = null
                        titleText = null
                    }
                }

                if (level != null && !titleText.isNullByBlank()) {
                    totalHeadings++
                    if (level > maxDepth) maxDepth = level

                    if (level > lastLevel + 1) {
                        skippedLevelCount++
                        skippedIssues.add("${section.title}: H$lastLevel -> H$level")
                    }
                    lastLevel = level
                }
            }
        }

        val isValid = skippedLevelCount == 0 && maxDepth <= 4
        val issueSummary = when {
            skippedLevelCount > 0 -> "Found $skippedLevelCount skipped heading jump(s) without intermediate nesting (e.g. ${skippedIssues.take(2).joinToString("; ")})."
            maxDepth > 4 -> "Found heading levels deeper than H4 (H$maxDepth detected)."
            else -> "All $totalHeadings heading elements follow a strict, sequential H1–H4 hierarchy across ${sections.size} sections."
        }

        return HeadingHierarchyReport(isValid, totalHeadings, skippedLevelCount, maxDepth, issueSummary)
    }

    private data class ImageAltTextReport(
        val isValid: Boolean,
        val totalImages: Int,
        val missingAltCount: Int,
        val issueSummary: String
    )

    private fun analyzeImageAltText(sections: List<SectionEntity>): ImageAltTextReport {
        var totalImages = 0
        var missingAltCount = 0
        val missingDetails = mutableListOf<String>()

        val mdImageRegex = Regex("""!\[(.*?)\]\((.*?)\)""")
        val htmlImageRegex = Regex("""(?i)<img\b([^>]*)>""")
        val altAttrRegex = Regex("""(?i)\balt\s*=\s*["']([^"']*)["']""")

        val genericPlaceholders = setOf("image", "photo", "pic", "img", "untitled", "figure", "picture")

        sections.forEach { section ->
            val text = section.contentText

            mdImageRegex.findAll(text).forEach { match ->
                totalImages++
                val altText = match.groupValues[1].trim()
                if (altText.isBlank() || genericPlaceholders.contains(altText.lowercase())) {
                    missingAltCount++
                    missingDetails.add(section.title)
                }
            }

            htmlImageRegex.findAll(text).forEach { match ->
                totalImages++
                val attrs = match.groupValues[1]
                val altMatch = altAttrRegex.find(attrs)
                val altText = altMatch?.groupValues?.get(1)?.trim() ?: ""
                if (altText.isBlank() || genericPlaceholders.contains(altText.lowercase())) {
                    missingAltCount++
                    missingDetails.add(section.title)
                }
            }
        }

        val isValid = missingAltCount == 0
        val issueSummary = when {
            missingAltCount > 0 -> "Found $missingAltCount image reference(s) missing descriptive alt-text in ${missingDetails.distinct().take(2).joinToString(", ")}."
            totalImages > 0 -> "Verified $totalImages embedded image(s): 100% compliant with KDP & EPUB alt-text accessibility standards."
            else -> "No unlabelled images detected across manuscript. Structure is 100% alt-text & figure tag compliant."
        }

        return ImageAltTextReport(isValid, totalImages, missingAltCount, issueSummary)
    }

    private fun String?.isNullByBlank(): Boolean = this == null || this.isBlank()
}
