package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.KdpPagePaperView
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.PaperCream
import com.example.ui.theme.PaperText
import com.example.ui.viewmodel.BookViewModel
import com.example.ui.viewmodel.PreviewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProofPreviewScreen(
    viewModel: BookViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val book by viewModel.book.collectAsState()
    val sections by viewModel.sections.collectAsState()
    val previewMode by viewModel.previewMode.collectAsState()

    var currentPageIndex by remember { mutableStateOf(0) }
    var showGutterOverlay by remember { mutableStateOf(true) }

    val currentBook = book ?: return
    val currentSections = sections.ifEmpty { return }

    val activeSection = currentSections.getOrNull(currentPageIndex % currentSections.size) ?: currentSections.first()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("6\" × 9\" KDP Proof Simulator", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("proof_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showGutterOverlay = !showGutterOverlay },
                        modifier = Modifier.testTag("toggle_gutter_button")
                    ) {
                        Icon(
                            imageVector = if (showGutterOverlay) Icons.Default.GridOn else Icons.Default.GridOff,
                            contentDescription = "Toggle Gutter Line",
                            tint = if (showGutterOverlay) GoldPrimary else MaterialTheme.colorScheme.secondary
                        )
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
            // Mode Selector Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = previewMode == PreviewMode.PAPERBACK_6X9,
                        onClick = { viewModel.setPreviewMode(PreviewMode.PAPERBACK_6X9) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                    ) {
                        Text("Paperback (6\"×9\")", fontSize = 11.sp)
                    }
                    SegmentedButton(
                        selected = previewMode == PreviewMode.KINDLE_EBOOK,
                        onClick = { viewModel.setPreviewMode(PreviewMode.KINDLE_EBOOK) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                    ) {
                        Text("Kindle eBook", fontSize = 11.sp)
                    }
                    SegmentedButton(
                        selected = previewMode == PreviewMode.WEB_VERSION,
                        onClick = { viewModel.setPreviewMode(PreviewMode.WEB_VERSION) },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                        modifier = Modifier.testTag("preview_mode_web_button")
                    ) {
                        Text("Web Edition", fontSize = 11.sp)
                    }
                }
            }

            // Page Navigation Controller
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (currentPageIndex > 0) currentPageIndex-- },
                    enabled = currentPageIndex > 0,
                    modifier = Modifier.testTag("prev_page_button")
                ) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Page")
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Page ${currentPageIndex + 1} of ${currentSections.size}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = activeSection.title,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1
                    )
                }

                IconButton(
                    onClick = { if (currentPageIndex < currentSections.size - 1) currentPageIndex++ },
                    enabled = currentPageIndex < currentSections.size - 1,
                    modifier = Modifier.testTag("next_page_button")
                ) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Page")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Proof Page Area
            when (previewMode) {
                PreviewMode.PAPERBACK_6X9 -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        KdpPagePaperView(
                            book = currentBook,
                            section = activeSection,
                            pageIndex = currentPageIndex,
                            showMarginsOverlay = showGutterOverlay,
                            modifier = Modifier.widthIn(max = 340.dp)
                        )
                    }
                }
                PreviewMode.KINDLE_EBOOK -> {
                    // Kindle eBook View
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PaperCream),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    text = activeSection.title,
                                    fontSize = 22.sp,
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold,
                                    color = PaperText
                                )
                                if (activeSection.subtitle.isNotBlank()) {
                                    Text(
                                        text = activeSection.subtitle,
                                        fontSize = 16.sp,
                                        fontFamily = FontFamily.Serif,
                                        color = Color(0xFF52525B)
                                    )
                                }
                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            }

                            item {
                                Text(
                                    text = activeSection.contentText,
                                    fontSize = 16.sp,
                                    fontFamily = FontFamily.Serif,
                                    lineHeight = 24.sp,
                                    color = PaperText
                                )
                            }
                        }
                    }
                }
                PreviewMode.WEB_VERSION -> {
                    // Web Edition View
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF7F2)),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Web Header bar
                            Surface(
                                color = Color(0xFFF3EDD7),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Language,
                                            contentDescription = null,
                                            tint = GoldPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Web Cloud Reader",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF2B2B2B)
                                        )
                                    }

                                    Surface(
                                        color = GoldPrimary,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "HTML5 Web Ready",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = Color(0xFFE2DCD2))

                            LazyColumn(
                                contentPadding = PaddingValues(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item {
                                    Text(
                                        text = activeSection.title,
                                        fontSize = 24.sp,
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111111)
                                    )
                                    if (activeSection.subtitle.isNotBlank()) {
                                        Text(
                                            text = activeSection.subtitle,
                                            fontSize = 15.sp,
                                            fontFamily = FontFamily.Serif,
                                            color = Color(0xFF666666)
                                        )
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE2DCD2))
                                }

                                item {
                                    Text(
                                        text = activeSection.contentText,
                                        fontSize = 17.sp,
                                        fontFamily = FontFamily.Serif,
                                        lineHeight = 26.sp,
                                        color = Color(0xFF2B2B2B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
