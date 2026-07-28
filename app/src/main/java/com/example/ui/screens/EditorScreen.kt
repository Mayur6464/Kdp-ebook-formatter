package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.SectionCategory
import com.example.data.SectionEntity
import com.example.formatter.KdpTextAnalysisEngine
import com.example.formatter.TextAnalysisResult
import com.example.ui.components.CloudSyncBadge
import com.example.ui.components.KdpPagePaperView
import com.example.ui.components.KdpTextAnalysisBottomSheet
import com.example.ui.theme.GoldPrimary
import com.example.ui.viewmodel.BookViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: BookViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val book by viewModel.book.collectAsState()
    val sections by viewModel.sections.collectAsState()
    val selectedSectionId by viewModel.selectedSectionId.collectAsState()
    val cloudSyncStatus by viewModel.cloudSyncStatus.collectAsState()
    val isSyncingCloud by viewModel.isSyncingCloud.collectAsState()

    var activeCategory by remember { mutableStateOf(SectionCategory.MAIN_CONTENT) }
    var showAddChapterDialog by remember { mutableStateOf(false) }
    var showLivePreview by remember { mutableStateOf(false) }
    var selectedPaperStock by remember { mutableStateOf("Cream") } // "Cream", "White", "Dark"

    val paperBgColor = when (selectedPaperStock) {
        "White" -> Color(0xFFFFFFFF)
        "Dark" -> Color(0xFF1E293B)
        else -> Color(0xFFFDF8EB) // Cream (55lb)
    }

    val paperTextColor = when (selectedPaperStock) {
        "Dark" -> Color(0xFFF1F5F9)
        else -> Color(0xFF2D2A26)
    }

    val filteredSections = sections.filter { it.sectionType.category == activeCategory }
    val currentSection = sections.find { it.id == selectedSectionId } ?: filteredSections.firstOrNull() ?: sections.firstOrNull()

    var editTitle by remember(currentSection?.id) { mutableStateOf(currentSection?.title ?: "") }
    var editSubtitle by remember(currentSection?.id) { mutableStateOf(currentSection?.subtitle ?: "") }
    var editEpigraph by remember(currentSection?.id) { mutableStateOf(currentSection?.epigraph ?: "") }
    var editContentText by remember(currentSection?.id) { mutableStateOf(currentSection?.contentText ?: "") }
    var editInToc by remember(currentSection?.id) { mutableStateOf(currentSection?.isIncludedInToc ?: true) }

    // Gemini Text Analysis State
    var showTextAnalysisSheet by remember { mutableStateOf(false) }
    var analysisResult by remember { mutableStateOf<TextAnalysisResult?>(null) }
    var isAnalyzingText by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val triggerTextAnalysis = {
        if (editContentText.isNotBlank()) {
            isAnalyzingText = true
            showTextAnalysisSheet = true
            coroutineScope.launch {
                analysisResult = KdpTextAnalysisEngine.analyzeText(editContentText)
                isAnalyzingText = false
            }
        }
    }

    // Real-Time Word Count & Reading Time Metrics (KDP 250 WPM Standard)
    val currentSectionWordCount = remember(editContentText) {
        if (editContentText.isBlank()) 0
        else editContentText.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
    }

    val totalBookWordCount = remember(sections, currentSection?.id, editContentText) {
        val otherSectionsWords = sections
            .filter { it.id != currentSection?.id }
            .sumOf { sec ->
                if (sec.contentText.isBlank()) 0
                else sec.contentText.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }.size
            }
        otherSectionsWords + currentSectionWordCount
    }

    val currentReadingTimeMinutes = (currentSectionWordCount / 250.0f).let { if (it > 0 && it < 1) 1 else kotlin.math.round(it).toInt() }
    val totalReadingTimeMinutes = (totalBookWordCount / 250.0f).let { if (it > 0 && it < 1) 1 else kotlin.math.round(it).toInt() }
    val estimatedKdpPages = String.format("%.1f", totalBookWordCount / 280.0f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Manuscript Editor", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = "$totalBookWordCount words • ~$totalReadingTimeMinutes min read ($estimatedKdpPages KDP pgs)",
                            fontSize = 11.sp,
                            color = GoldPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("editor_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    CloudSyncBadge(
                        statusText = cloudSyncStatus,
                        isSyncing = isSyncingCloud,
                        onTriggerSync = { viewModel.triggerManualCloudSync() }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { triggerTextAnalysis() },
                        modifier = Modifier.testTag("gemini_text_analyzer_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini Text & Readability Analyzer",
                            tint = GoldPrimary
                        )
                    }
                    IconButton(
                        onClick = { showLivePreview = !showLivePreview },
                        modifier = Modifier.testTag("toggle_live_preview_button")
                    ) {
                        Icon(
                            imageVector = if (showLivePreview) Icons.Default.VisibilityOff else Icons.Default.MenuBook,
                            contentDescription = "Toggle Live KDP Paper Preview",
                            tint = if (showLivePreview) GoldPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { showAddChapterDialog = true },
                        modifier = Modifier.testTag("add_chapter_dialog_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add New Chapter", tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category Filter Tabs
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(SectionCategory.values()) { category ->
                    val isSelected = activeCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { activeCategory = category },
                        label = {
                            Text(
                                when (category) {
                                    SectionCategory.FRONT_MATTER -> "Front Matter"
                                    SectionCategory.MAIN_CONTENT -> "Chapters (11)"
                                    SectionCategory.CONCLUSION_APPENDIX -> "Conclusion & Extras"
                                    SectionCategory.BACK_MATTER -> "Back Matter"
                                },
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Section Selector Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(filteredSections) { sec ->
                    val isSelected = sec.id == currentSection?.id
                    AssistChip(
                        onClick = { viewModel.selectSection(sec.id) },
                        label = { Text(sec.title, fontSize = 11.sp, maxLines = 1) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (isSelected) GoldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            // Real-Time Word Count & Reading Time Metrics Status Bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp)
                    .testTag("kdp_realtime_metrics_bar")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Current Chapter: $currentSectionWordCount words (~$currentReadingTimeMinutes min read)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Total Manuscript: $totalBookWordCount words (~$totalReadingTimeMinutes min total read • ~$estimatedKdpPages pgs)",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = GoldPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .clickable { triggerTextAnalysis() }
                                .testTag("trigger_ai_quality_check_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "AI Readability Check",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Active Section Form
            if (currentSection != null) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Live Paper Stock Preview Card Overlay
                    if (showLivePreview) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("live_paper_preview_card")
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = GoldPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "KDP Print Paper Overlay",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        // Paper Stock Selector Chips
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            FilterChip(
                                                selected = selectedPaperStock == "Cream",
                                                onClick = { selectedPaperStock = "Cream" },
                                                label = { Text("Cream 55lb", fontSize = 10.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Color(0xFFFDF8EB),
                                                    selectedLabelColor = Color(0xFF2D2A26)
                                                ),
                                                modifier = Modifier.testTag("paper_stock_cream")
                                            )
                                            FilterChip(
                                                selected = selectedPaperStock == "White",
                                                onClick = { selectedPaperStock = "White" },
                                                label = { Text("White 50lb", fontSize = 10.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Color.White,
                                                    selectedLabelColor = Color.Black
                                                ),
                                                modifier = Modifier.testTag("paper_stock_white")
                                            )
                                            FilterChip(
                                                selected = selectedPaperStock == "Dark",
                                                onClick = { selectedPaperStock = "Dark" },
                                                label = { Text("Kindle Dark", fontSize = 10.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = Color(0xFF1E293B),
                                                    selectedLabelColor = Color.White
                                                ),
                                                modifier = Modifier.testTag("paper_stock_dark")
                                            )
                                        }
                                    }

                                    book?.let { currentBook ->
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            KdpPagePaperView(
                                                book = currentBook,
                                                section = currentSection.copy(
                                                    title = editTitle,
                                                    subtitle = editSubtitle,
                                                    epigraph = editEpigraph,
                                                    contentText = editContentText
                                                ),
                                                pageIndex = 0,
                                                showMarginsOverlay = true,
                                                paperStockBgColor = paperBgColor,
                                                paperStockTextColor = paperTextColor,
                                                modifier = Modifier
                                                    .fillMaxWidth(0.85f)
                                                    .padding(vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = editTitle,
                            onValueChange = {
                                editTitle = it
                                viewModel.updateSection(currentSection.copy(title = it))
                            },
                            label = { Text("Section Title (Heading 1)") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_section_title")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = editSubtitle,
                            onValueChange = {
                                editSubtitle = it
                                viewModel.updateSection(currentSection.copy(subtitle = it))
                            },
                            label = { Text("Subtitle / Heading 2") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_section_subtitle")
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = editEpigraph,
                            onValueChange = {
                                editEpigraph = it
                                viewModel.updateSection(currentSection.copy(epigraph = it))
                            },
                            label = { Text("Epigraph / Chapter Quote") },
                            modifier = Modifier.fillMaxWidth().testTag("edit_section_epigraph")
                        )
                    }

                    // Formatting Quick Toolbar
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Quick Format:", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            SuggestionChip(
                                onClick = {
                                    editContentText += "\n\n## Subheading Title\n"
                                    viewModel.updateSection(currentSection.copy(contentText = editContentText))
                                },
                                label = { Text("## Heading 2", fontSize = 11.sp) }
                            )
                            SuggestionChip(
                                onClick = {
                                    editContentText += "\n\n• Key Takeaway Point\n"
                                    viewModel.updateSection(currentSection.copy(contentText = editContentText))
                                },
                                label = { Text("• Bullet Point", fontSize = 11.sp) }
                            )
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = editContentText,
                            onValueChange = {
                                editContentText = it
                                viewModel.updateSection(currentSection.copy(contentText = it))
                            },
                            label = { Text("Content Body Text (Georgia 12pt)") },
                            minLines = 10,
                            modifier = Modifier.fillMaxWidth().testTag("edit_section_body")
                        )
                    }

                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = editInToc,
                                    onCheckedChange = {
                                        editInToc = it
                                        viewModel.updateSection(currentSection.copy(isIncludedInToc = it))
                                    }
                                )
                                Text("Include in Table of Contents", fontSize = 13.sp)
                            }

                            if (currentSection.sectionType == SectionCategory.MAIN_CONTENT.run { com.example.data.SectionType.CHAPTER }) {
                                TextButton(
                                    onClick = { viewModel.deleteSection(currentSection) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete Chapter", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Chapter Dialog
        if (showAddChapterDialog) {
            var newTitle by remember { mutableStateOf("") }
            var newSubtitle by remember { mutableStateOf("") }
            var newBody by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showAddChapterDialog = false },
                title = { Text("Add New Chapter") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = { Text("Chapter Title") }
                        )
                        OutlinedTextField(
                            value = newSubtitle,
                            onValueChange = { newSubtitle = it },
                            label = { Text("Chapter Subtitle") }
                        )
                        OutlinedTextField(
                            value = newBody,
                            onValueChange = { newBody = it },
                            label = { Text("Initial Body Text") },
                            minLines = 3
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newTitle.isNotBlank()) {
                                viewModel.addNewChapter(newTitle, newSubtitle, newBody)
                                showAddChapterDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        Text("Add Chapter")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddChapterDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showTextAnalysisSheet) {
            KdpTextAnalysisBottomSheet(
                analysisResult = analysisResult,
                isAnalyzing = isAnalyzingText,
                onDismissRequest = { showTextAnalysisSheet = false },
                onApplyFix = { originalText, replacementText ->
                    if (editContentText.contains(originalText)) {
                        val updatedText = editContentText.replace(originalText, replacementText)
                        editContentText = updatedText
                        if (currentSection != null) {
                            viewModel.updateSection(currentSection.copy(contentText = updatedText))
                        }
                    }
                },
                onReAnalyze = { triggerTextAnalysis() }
            )
        }
    }
}
