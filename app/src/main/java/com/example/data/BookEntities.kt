package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SectionCategory {
    FRONT_MATTER,
    MAIN_CONTENT,
    CONCLUSION_APPENDIX,
    BACK_MATTER
}

enum class SectionType(val displayName: String, val defaultTitle: String, val category: SectionCategory) {
    COPYRIGHT("Copyright Page", "Copyright Page", SectionCategory.FRONT_MATTER),
    MEDICAL_DISCLAIMER("Medical Disclaimer", "Medical Disclaimer", SectionCategory.FRONT_MATTER),
    AFFILIATE_DISCLOSURE("Affiliate Disclosure", "Affiliate Disclosure", SectionCategory.FRONT_MATTER),
    TABLE_OF_CONTENTS("Table of Contents", "Table of Contents", SectionCategory.FRONT_MATTER),
    INTRODUCTION("Introduction", "Introduction", SectionCategory.FRONT_MATTER),

    CHAPTER("Chapter", "Chapter", SectionCategory.MAIN_CONTENT),

    CONCLUSION("Conclusion", "Conclusion", SectionCategory.CONCLUSION_APPENDIX),
    RECOMMENDED_RESOURCES("Recommended Resources", "Recommended Resources", SectionCategory.CONCLUSION_APPENDIX),
    APPENDIX("Appendix", "Appendix", SectionCategory.CONCLUSION_APPENDIX),

    ABOUT_AUTHOR("About the Author", "About the Author", SectionCategory.BACK_MATTER),
    THANK_YOU("Thank You Page", "Thank You", SectionCategory.BACK_MATTER),
    AMAZON_REVIEW("Amazon Review Invitation", "Invitation to Leave an Amazon Review", SectionCategory.BACK_MATTER),
    OTHER_BOOKS("Other Books by SAMNJ | Glowlist", "Other Books by SAMNJ | Glowlist", SectionCategory.BACK_MATTER),
    WEBSITE_SOCIAL("Website & Social Media", "Connect With Us", SectionCategory.BACK_MATTER)
}

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "Glowlist: The Ultimate Guide to Radiant Living",
    val subtitle: String = "11 Essential Chapters for Wellness & Growth",
    val author: String = "SAMNJ | Glowlist",
    val publisher: String = "Glowlist Publishing",
    val isbn: String = "978-1-987654-32-1",
    val publicationYear: Int = 2026,
    
    // Formatting Specifications
    val trimWidthInches: Float = 6.0f,
    val trimHeightInches: Float = 9.0f,
    val marginTopInches: Float = 1.0f,
    val marginBottomInches: Float = 1.0f,
    val marginLeftInches: Float = 1.0f,
    val marginRightInches: Float = 1.0f,
    val gutterInches: Float = 0.125f,
    
    // Typography
    val bodyFontFamily: String = "Georgia",
    val bodyFontSizePt: Int = 12,
    val chapterTitleSizePt: Int = 20,
    val heading2SizePt: Int = 15,
    val lineSpacingMultiplier: Float = 1.15f,
    
    // Layout Toggles
    val enableRunningHeaders: Boolean = true,
    val enablePageNumbers: Boolean = true,
    val startPageNumbersAfterFrontMatter: Boolean = true,
    val widowOrphanControl: Boolean = true,
    val cleanPageBreaksBeforeChapters: Boolean = true,

    // Cover Generator & Cloud Sync Specifications
    val coverGenre: String = "Non-Fiction / Wellness",
    val coverBlurb: String = "Discover 11 transformative daily routines designed to awaken vitality, mindfulness, and inner radiance. Glowlist brings together science-backed wellness habits and holistic lifestyle practices.",
    val coverStyle: String = "Modern Gold & Navy",
    val coverPaperType: String = "Cream 55lb",
    val coverBgColorHex: String = "#1E293B",
    val coverTextColorHex: String = "#F8FAFC",
    val lastCloudSyncedAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "sections")
data class SectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: Long = 1,
    val sectionType: SectionType,
    val chapterNumber: Int? = null, // e.g. 1 to 11 for CHAPTER
    val orderIndex: Int,
    val title: String,
    val subtitle: String = "",
    val epigraph: String = "",
    val contentText: String,
    val pageBreakBefore: Boolean = true,
    val isIncludedInToc: Boolean = true
)
