package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.SectionCategory
import com.example.ui.components.CloudSyncBadge
import com.example.ui.components.ExportSettingsBottomSheet
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: BookViewModel,
    onNavigateToEditor: () -> Unit,
    onNavigateToProof: () -> Unit,
    onNavigateToChecklist: () -> Unit,
    onNavigateToExport: () -> Unit,
    onNavigateToCover: () -> Unit = {},
    onNavigateToMonetization: () -> Unit = {},
    onNavigateToReferral: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val book by viewModel.book.collectAsState()
    val sections by viewModel.sections.collectAsState()
    val auditResult by viewModel.auditResult.collectAsState()
    val cloudSyncStatus by viewModel.cloudSyncStatus.collectAsState()
    val isSyncingCloud by viewModel.isSyncingCloud.collectAsState()

    val currentBook = book ?: return

    var showExportSettingsSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "App Icon",
                            tint = GoldPrimary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column {
                            Text(
                                "KDP Formatter",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Professional Book Publishing Suite",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
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
                        onClick = { showExportSettingsSheet = true },
                        modifier = Modifier.testTag("open_export_settings_dashboard_top")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Export Settings & Data Ingestion",
                            tint = GoldPrimary
                        )
                    }
                    IconButton(
                        onClick = { viewModel.resetToDefaults() },
                        modifier = Modifier.testTag("reset_defaults_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Book Defaults",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding() + 16.dp,
                bottom = paddingValues.calculateBottomPadding() + 24.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Hero Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Navy800),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_banner_1785153612386),
                            contentDescription = "Publishing Hero Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0x990F172A))
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.align(Alignment.BottomStart)
                            ) {
                                Surface(
                                    color = GoldPrimary,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "KDP 6\" × 9\" STANDARDS",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentBook.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "By ${currentBook.author}",
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // KDP Quality Status Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        if ((auditResult?.scorePercentage ?: 0) == 100) SuccessGreen.copy(alpha = 0.15f) else GoldPrimary.copy(alpha = 0.15f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = if ((auditResult?.scorePercentage ?: 0) == 100) Icons.Default.Verified else Icons.Default.FactCheck,
                                    contentDescription = "Audit Status",
                                    tint = if ((auditResult?.scorePercentage ?: 0) == 100) SuccessGreen else GoldPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${auditResult?.scorePercentage ?: 100}% KDP Ready",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${auditResult?.passedChecksCount ?: 12} of ${auditResult?.totalChecks ?: 12} formatting checks passed",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        Button(
                            onClick = onNavigateToChecklist,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            modifier = Modifier.testTag("audit_checklist_button")
                        ) {
                            Text("Audit")
                        }
                    }
                }
            }

            // Book Specification Matrix
            item {
                Text(
                    text = "FORMATTING SPECIFICATIONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SpecChip("Trim Size", "${currentBook.trimWidthInches.toInt()}\" × ${currentBook.trimHeightInches.toInt()}\"", Modifier.weight(1f))
                    SpecChip("Margins", "${currentBook.marginTopInches.toInt()}\" + Gutter", Modifier.weight(1f))
                    SpecChip("Typography", "${currentBook.bodyFontFamily} ${currentBook.bodyFontSizePt}pt", Modifier.weight(1f))
                }
            }

            // Quick Actions Hub
            item {
                Text(
                    text = "PUBLISHING ACTIONS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionRowCard(
                        title = "Chapter & Content Editor",
                        subtitle = "Edit 11 chapters, front matter & back matter",
                        icon = Icons.Default.Edit,
                        tag = "nav_editor_button",
                        onClick = onNavigateToEditor
                    )
                    ActionRowCard(
                        title = "Live 6\" × 9\" Proof Simulator",
                        subtitle = "Paperback print preview & Kindle reflow mode",
                        icon = Icons.Default.AutoStories,
                        tag = "nav_proof_button",
                        onClick = onNavigateToProof
                    )
                    ActionRowCard(
                        title = "AI KDP Book Cover Studio",
                        subtitle = "Synthesize 6x9 paperback print wrap with spine & barcode zone",
                        icon = Icons.Default.AutoAwesome,
                        tag = "nav_cover_studio_button",
                        onClick = onNavigateToCover
                    )
                    ActionRowCard(
                        title = "KDP Monetization & Royalty Studio",
                        subtitle = "Calculate author passive profit, list pricing, KENP & direct sales",
                        icon = Icons.Default.MonetizationOn,
                        tag = "nav_monetization_studio_button",
                        onClick = onNavigateToMonetization
                    )
                    ActionRowCard(
                        title = "Author Referral & Creator Rewards",
                        subtitle = "Invite fellow authors, generate unique links & earn $25 cash bonus + AI credits",
                        icon = Icons.Default.CardGiftcard,
                        tag = "nav_referral_dashboard_button",
                        onClick = onNavigateToReferral
                    )
                    ActionRowCard(
                        title = "100% KDP Quality Checklist",
                        subtitle = "Automated audit of 12 standard KDP criteria",
                        icon = Icons.Default.FactCheck,
                        tag = "nav_checklist_button",
                        onClick = onNavigateToChecklist
                    )
                    ActionRowCard(
                        title = "Export Delivery Suite",
                        subtitle = "Generate KDP Print (.docx), Kindle, PDF & Google Docs",
                        icon = Icons.Default.IosShare,
                        tag = "nav_export_button",
                        onClick = onNavigateToExport
                    )
                    ActionRowCard(
                        title = "Export Settings & Data Ingestion",
                        subtitle = "Toggle Paperback vs Kindle presets & upload or paste content",
                        icon = Icons.Default.Tune,
                        tag = "nav_export_settings_button",
                        onClick = { showExportSettingsSheet = true }
                    )
                }
            }

            // Section Breakdown Overview
            item {
                Text(
                    text = "MANUSCRIPT STRUCTURE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SectionCountRow("Front Matter", sections.count { it.sectionType.category == SectionCategory.FRONT_MATTER })
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        SectionCountRow("Main Content (11 Chapters)", sections.count { it.sectionType.category == SectionCategory.MAIN_CONTENT })
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        SectionCountRow("Conclusion & Extras", sections.count { it.sectionType.category == SectionCategory.CONCLUSION_APPENDIX })
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        SectionCountRow("Back Matter", sections.count { it.sectionType.category == SectionCategory.BACK_MATTER })
                    }
                }
            }
        }

        if (showExportSettingsSheet) {
            ExportSettingsBottomSheet(
                viewModel = viewModel,
                onDismissRequest = { showExportSettingsSheet = false }
            )
        }
    }
}

@Composable
fun SpecChip(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun ActionRowCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tag: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .background(GoldPrimary.copy(alpha = 0.15f), shape = CircleShape)
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = GoldPrimary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Go", tint = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun SectionCountRow(title: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        Surface(
            color = GoldPrimary.copy(alpha = 0.15f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "$count sections",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GoldPrimary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}
