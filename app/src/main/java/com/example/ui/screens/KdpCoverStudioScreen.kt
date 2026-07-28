package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.BookEntity
import com.example.ui.components.CloudSyncBadge
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.BookViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun KdpCoverStudioScreen(
    viewModel: BookViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val book by viewModel.book.collectAsState()
    val sections by viewModel.sections.collectAsState()
    val cloudSyncStatus by viewModel.cloudSyncStatus.collectAsState()
    val isSyncingCloud by viewModel.isSyncingCloud.collectAsState()

    val currentBook = book ?: return

    val totalWordCount = sections.sumOf { it.contentText.split("\\s+".toRegex()).size }
    val estimatedPages = (totalWordCount / 280.0).roundToInt().coerceAtLeast(sections.size * 2)
    val paperThickness = if (currentBook.coverPaperType.contains("Cream")) 0.00225f else 0.00225f
    val spineWidthInches = estimatedPages * paperThickness

    var showGuides by remember { mutableStateOf(true) }
    var selectedGenre by remember { mutableStateOf(currentBook.coverGenre) }
    var blurbText by remember { mutableStateOf(currentBook.coverBlurb) }
    var titleText by remember { mutableStateOf(currentBook.title) }
    var subtitleText by remember { mutableStateOf(currentBook.subtitle) }
    var authorText by remember { mutableStateOf(currentBook.author) }
    var publisherText by remember { mutableStateOf(currentBook.publisher) }
    var isbnText by remember { mutableStateOf(currentBook.isbn) }

    val genres = listOf(
        "Non-Fiction / Wellness",
        "Sci-Fi & Fantasy",
        "Romance & Drama",
        "Business & Finance",
        "Mystery & Thriller",
        "Self-Help & Growth"
    )

    // Parse background color
    val coverBgColor = try {
        Color(android.graphics.Color.parseColor(currentBook.coverBgColorHex))
    } catch (e: Exception) {
        Navy900
    }

    val coverTextColor = try {
        Color(android.graphics.Color.parseColor(currentBook.coverTextColorHex))
    } catch (e: Exception) {
        Color.White
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI KDP Cover Studio",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Navy900
                        )
                        Text(
                            text = "Full 6x9 Paperback Print Wrap Template",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("cover_studio_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Navy900)
                    }
                },
                actions = {
                    CloudSyncBadge(
                        statusText = cloudSyncStatus,
                        isSyncing = isSyncingCloud,
                        onTriggerSync = { viewModel.triggerManualCloudSync() }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Navy800),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("6\" x 9\" KDP Cover Specification", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Page Count: $estimatedPages pgs • Spine Width: ${String.format("%.3f", spineWidthInches)}\" (${currentBook.coverPaperType})",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Includes 0.125\" Bleed, Barcode Safety Zone & 0.375\" Trim Zone",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 10.sp
                        )
                    }

                    FilterChip(
                        selected = showGuides,
                        onClick = { showGuides = !showGuides },
                        label = { Text(if (showGuides) "Guides ON" else "Guides OFF", fontSize = 10.sp) },
                        leadingIcon = { Icon(Icons.Default.SquareFoot, contentDescription = null, modifier = Modifier.size(12.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = Navy900
                        ),
                        modifier = Modifier.testTag("toggle_cover_guides_button")
                    )
                }
            }

            // Interactive Full Cover Wrap Preview (Back Cover | Spine | Front Cover)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth().testTag("kdp_cover_wrap_card")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "FULL 6x9 PAPERBACK COVER WRAP PREVIEW",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .shadow(6.dp, RoundedCornerShape(6.dp))
                            .clip(RoundedCornerShape(6.dp))
                            .background(coverBgColor)
                            .border(
                                width = if (showGuides) 1.dp else 0.dp,
                                color = if (showGuides) Color(0xFFEF4444) else Color.Transparent,
                                shape = RoundedCornerShape(6.dp)
                            )
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            // 1. BACK COVER (Left)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(coverBgColor, coverBgColor.copy(alpha = 0.85f))
                                        )
                                    )
                                    .border(
                                        width = if (showGuides) 0.5.dp else 0.dp,
                                        color = if (showGuides) Color(0xFFE2E8F0).copy(alpha = 0.3f) else Color.Transparent
                                    )
                                    .padding(8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            text = titleText,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            color = GoldPrimary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = authorText,
                                            fontSize = 8.sp,
                                            color = coverTextColor.copy(alpha = 0.8f)
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = blurbText,
                                            fontSize = 7.5.sp,
                                            color = coverTextColor.copy(alpha = 0.9f),
                                            lineHeight = 10.sp,
                                            fontFamily = FontFamily.Serif,
                                            maxLines = 10,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Column {
                                            Text(
                                                text = publisherText,
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = GoldPrimary
                                            )
                                            Text(
                                                text = "ISBN $isbnText",
                                                fontSize = 6.sp,
                                                color = coverTextColor.copy(alpha = 0.6f)
                                            )
                                        }

                                        // Official KDP Barcode Zone Placeholder (2.0" x 1.2" safe zone)
                                        Surface(
                                            color = Color.White,
                                            shape = RoundedCornerShape(2.dp),
                                            border = if (showGuides) BorderStroke(1.dp, Color(0xFFF59E0B)) else null,
                                            modifier = Modifier.size(width = 54.dp, height = 32.dp)
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center,
                                                modifier = Modifier.padding(2.dp)
                                            ) {
                                                Icon(Icons.Default.QrCode2, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                                Text("KDP BARCODE", fontSize = 5.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            // 2. SPINE (Center)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .width(28.dp)
                                    .fillMaxHeight()
                                    .background(coverBgColor.copy(alpha = 0.95f))
                                    .border(
                                        width = if (showGuides) 0.5.dp else 0.dp,
                                        color = if (showGuides) GoldPrimary.copy(alpha = 0.5f) else Color.Transparent
                                    )
                                    .padding(vertical = 8.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    Text(
                                        text = titleText.take(20),
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldPrimary,
                                        modifier = Modifier.rotate(90f)
                                    )
                                    Text(
                                        text = authorText,
                                        fontSize = 6.sp,
                                        color = coverTextColor,
                                        modifier = Modifier.rotate(90f)
                                    )
                                }
                            }

                            // 3. FRONT COVER (Right)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .background(coverBgColor)
                            ) {
                                // Background texture
                                Image(
                                    painter = painterResource(id = R.drawable.img_kdp_cover_bg_1785155688446),
                                    contentDescription = "Cover background art",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().alpha(0.85f)
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Genre Badge
                                    Surface(
                                        color = GoldPrimary.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = selectedGenre.uppercase(),
                                            fontSize = 6.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldPrimary,
                                            letterSpacing = 1.sp,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = titleText,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 13.sp,
                                            fontFamily = FontFamily.Serif,
                                            color = GoldPrimary,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 15.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = subtitleText,
                                            fontSize = 8.sp,
                                            fontFamily = FontFamily.Serif,
                                            fontStyle = FontStyle.Italic,
                                            color = coverTextColor.copy(alpha = 0.9f),
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        HorizontalDivider(
                                            color = GoldPrimary.copy(alpha = 0.5f),
                                            thickness = 0.5.dp,
                                            modifier = Modifier.width(30.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = authorText.uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 8.sp,
                                            color = coverTextColor,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Guide Overlay Legend
                        if (showGuides) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("■ Red: Bleed (0.125\")", fontSize = 7.sp, color = Color(0xFFEF4444))
                                Text("■ Gold: Spine Safety", fontSize = 7.sp, color = GoldPrimary)
                                Text("■ Yellow: Barcode Safety", fontSize = 7.sp, color = Color(0xFFF59E0B))
                            }
                        }
                    }
                }
            }

            // AI Cover Generator & Preset Controls
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("ai_cover_generator_controls_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Cover Style & Genre Synthesis", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.generateAiCoverConfigForGenre(selectedGenre)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("ai_generate_cover_button")
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(14.dp), tint = Navy900)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Auto Synth", fontSize = 11.sp, color = Navy900, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text("SELECT GENRE PRESET", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        genres.forEach { genre ->
                            FilterChip(
                                selected = selectedGenre == genre,
                                onClick = {
                                    selectedGenre = genre
                                    viewModel.generateAiCoverConfigForGenre(genre)
                                },
                                label = { Text(genre, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary,
                                    selectedLabelColor = Navy900
                                ),
                                modifier = Modifier.testTag("genre_chip_$genre")
                            )
                        }
                    }

                    HorizontalDivider()

                    Text("COVER TEXT & BLURB EDIT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)

                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        label = { Text("Book Title") },
                        modifier = Modifier.fillMaxWidth().testTag("cover_title_input")
                    )

                    OutlinedTextField(
                        value = subtitleText,
                        onValueChange = { subtitleText = it },
                        label = { Text("Subtitle") },
                        modifier = Modifier.fillMaxWidth().testTag("cover_subtitle_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = authorText,
                            onValueChange = { authorText = it },
                            label = { Text("Author") },
                            modifier = Modifier.weight(1f).testTag("cover_author_input")
                        )

                        OutlinedTextField(
                            value = publisherText,
                            onValueChange = { publisherText = it },
                            label = { Text("Publisher") },
                            modifier = Modifier.weight(1f).testTag("cover_publisher_input")
                        )
                    }

                    OutlinedTextField(
                        value = blurbText,
                        onValueChange = { blurbText = it },
                        label = { Text("Back Cover Blurb") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("cover_blurb_input")
                    )

                    // Apply and Save Button
                    Button(
                        onClick = {
                            viewModel.updateCoverSettings(
                                genre = selectedGenre,
                                blurb = blurbText,
                                style = currentBook.coverStyle,
                                paperType = currentBook.coverPaperType,
                                bgColorHex = currentBook.coverBgColorHex,
                                textColorHex = currentBook.coverTextColorHex
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                        modifier = Modifier.fillMaxWidth().testTag("save_cover_to_project_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = GoldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Cover Template & Trigger Cloud Sync", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
