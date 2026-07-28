package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.formatter.KdpCheckItem
import com.example.formatter.KdpCheckerEngine
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.NaturalTaupe
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChecklistScreen(
    viewModel: BookViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val book by viewModel.book.collectAsState()
    val sections by viewModel.sections.collectAsState()

    val currentBook = book ?: return

    val genreOptions = remember {
        listOf(
            "Non-Fiction / Wellness",
            "Poetry Collection",
            "Fiction / Novel",
            "Children's / Illustrated",
            "Academic / Technical"
        )
    }

    var activeGenre by remember(currentBook.coverGenre) { mutableStateOf(currentBook.coverGenre) }
    var activeCategoryFilter by remember { mutableStateOf("All") }

    val currentAudit = remember(currentBook, sections, activeGenre) {
        KdpCheckerEngine.auditBook(currentBook, sections, activeGenre)
    }

    val categories = remember(currentAudit) {
        listOf("All") + currentAudit.items.map { it.category }.distinct()
    }

    val filteredItems = if (activeCategoryFilter == "All") {
        currentAudit.items
    } else {
        currentAudit.items.filter { it.category == activeCategoryFilter }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("100% KDP Quality Checklist", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("checklist_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            viewModel.updateBook(
                                currentBook.copy(
                                    trimWidthInches = 6.0f,
                                    trimHeightInches = 9.0f,
                                    marginTopInches = 1.0f,
                                    marginBottomInches = 1.0f,
                                    marginLeftInches = 1.0f,
                                    marginRightInches = 1.0f,
                                    gutterInches = 0.125f,
                                    bodyFontFamily = "Georgia",
                                    bodyFontSizePt = if (activeGenre.contains("Children", ignoreCase = true)) 14 else 12,
                                    chapterTitleSizePt = 20,
                                    heading2SizePt = 15,
                                    enableRunningHeaders = true,
                                    enablePageNumbers = true,
                                    startPageNumbersAfterFrontMatter = true,
                                    widowOrphanControl = true,
                                    cleanPageBreaksBeforeChapters = true
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("auto_fix_all_button")
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Auto-Fix All", fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 12.dp,
                bottom = paddingValues.calculateBottomPadding() + 24.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Genre Validation Ruleset Selector Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Navy800),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("genre_ruleset_selector_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Category, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GENRE-SPECIFIC KDP VALIDATION RULESET",
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = "Select your book genre to apply tailored KDP compliance criteria (e.g. poetry lineation spacing, non-fiction index pages, or children's large print):",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            items(genreOptions) { g ->
                                val isSelected = activeGenre.equals(g, ignoreCase = true) || activeGenre.contains(g.take(5), ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        activeGenre = g
                                        viewModel.updateBook(currentBook.copy(coverGenre = g))
                                    },
                                    label = { Text(g, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GoldPrimary,
                                        selectedLabelColor = Navy900,
                                        containerColor = Color.White.copy(alpha = 0.15f),
                                        labelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("genre_rule_chip_${g.take(8).lowercase()}")
                                )
                            }
                        }
                    }
                }
            }

            // Score Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentAudit.scorePercentage == 100) SuccessGreen.copy(alpha = 0.12f) else WarningAmber.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    if (currentAudit.scorePercentage == 100) SuccessGreen else WarningAmber,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (currentAudit.scorePercentage == 100) Icons.Default.CheckCircle else Icons.Default.PriorityHigh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(34.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "${currentAudit.scorePercentage}% KDP Formatted",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = if (currentAudit.scorePercentage == 100)
                                "Your manuscript satisfies 100% of Amazon KDP Paperback & Kindle publishing criteria for $activeGenre!"
                            else
                                "Found ${currentAudit.totalChecks - currentAudit.passedChecksCount} criteria requiring attention before publishing as $activeGenre.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Digital Accessibility Analysis Feature Card
            item {
                val accessItems = currentAudit.items.filter { it.category == "Digital Accessibility" }
                val passedAccessCount = accessItems.count { it.isPassed }
                val isFullyAccessible = accessItems.isNotEmpty() && passedAccessCount == accessItems.size

                Card(
                    colors = CardDefaults.cardColors(containerColor = Navy900),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("accessibility_analysis_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccessibilityNew,
                                    contentDescription = "Accessibility",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DIGITAL ACCESSIBILITY ANALYSIS",
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp
                                )
                            }

                            Surface(
                                color = if (isFullyAccessible) SuccessGreen.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isFullyAccessible) "WCAG 2.1 COMPLIANT" else "$passedAccessCount/${accessItems.size} ACCESSIBLE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFullyAccessible) SuccessGreen else WarningAmber,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = "Automated audit of Kindle & EPUB digital accessibility standards: Heading Hierarchy (H1–H4), Image Alt-Text labels, and screen reader landmark tags.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Headings Hierarchy", fontSize = 9.sp, color = GoldPrimary, fontWeight = FontWeight.Bold)
                                    Text("H1–H4 Sequential", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Surface(
                                color = Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Image Alt-Text", fontSize = 9.sp, color = GoldPrimary, fontWeight = FontWeight.Bold)
                                    Text("Descriptive Figures", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Surface(
                                color = Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("Screen Reader", fontSize = 9.sp, color = GoldPrimary, fontWeight = FontWeight.Bold)
                                    Text("EPUB Landmarks", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        if (!isFullyAccessible) {
                            Button(
                                onClick = {
                                    viewModel.fixIndividualPitfall("access_heading_hierarchy")
                                    viewModel.fixIndividualPitfall("access_image_alt_text")
                                    viewModel.fixIndividualPitfall("access_screen_reader_landmarks")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.align(Alignment.End).testTag("fix_all_accessibility_button")
                            ) {
                                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Fix All Accessibility Issues", fontSize = 11.sp, color = Navy900)
                            }
                        }
                    }
                }
            }

            // Category Filter Bar
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = activeCategoryFilter == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { activeCategoryFilter = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NaturalTaupe,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Checks List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AUDIT SPECIFICATIONS (${currentAudit.passedChecksCount}/${currentAudit.totalChecks} PASSED)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp
                    )
                }
            }

            items(filteredItems) { checkItem ->
                ChecklistRowCard(
                    item = checkItem,
                    onFixClick = { viewModel.fixIndividualPitfall(checkItem.id) }
                )
            }
        }
    }
}

@Composable
fun ChecklistRowCard(
    item: KdpCheckItem,
    onFixClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (item.isPassed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (item.isPassed) SuccessGreen else WarningAmber,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (item.category.contains("Genre Rules") || item.category == "Digital Accessibility") {
                            Surface(
                                color = GoldPrimary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(bottom = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (item.category == "Digital Accessibility") Icons.Default.AccessibilityNew else Icons.Default.Category,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = item.category.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldPrimary
                                    )
                                }
                            }
                        }
                        Text(
                            text = item.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Surface(
                        color = if (item.isPassed) SuccessGreen.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (item.isPassed) "PASSED" else "NEEDS FIX",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isPassed) SuccessGreen else WarningAmber,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )

                if (!item.isPassed) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Recommendation: ${item.recommendation}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WarningAmber
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onFixClick,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.align(Alignment.End).testTag("fix_item_${item.id}")
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("1-Click Fix Issue", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
