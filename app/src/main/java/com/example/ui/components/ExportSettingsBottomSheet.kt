package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.NaturalTaupe
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.BookViewModel
import com.example.ui.viewmodel.PreviewMode
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportSettingsBottomSheet(
    viewModel: BookViewModel,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val book by viewModel.book.collectAsState()
    val previewMode by viewModel.previewMode.collectAsState()
    val isSyncingCloud by viewModel.isSyncingCloud.collectAsState()
    val cloudSyncStatus by viewModel.cloudSyncStatus.collectAsState()

    var activeInputTab by remember { mutableStateOf("paste") } // "file", "paste", "preset"

    // Paste state
    var pasteTitle by remember { mutableStateOf("") }
    var pasteSubtitle by remember { mutableStateOf("") }
    var pasteTextContent by remember { mutableStateOf("") }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                val inputStream = context.contentResolver.openInputStream(selectedUri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line).append("\n")
                }
                reader.close()

                val fileText = stringBuilder.toString()
                if (fileText.isNotBlank()) {
                    val fileName = selectedUri.lastPathSegment?.substringAfterLast("/") ?: "Imported File"
                    viewModel.importCustomData(
                        title = fileName.replace(".txt", "").replace(".md", "").replace("_", " "),
                        subtitle = "Imported Document",
                        contentText = fileText
                    )
                } else {
                    Toast.makeText(context, "Selected file is empty.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Failed to read file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = 8.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SettingsSuggest, contentDescription = null, tint = GoldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Export Settings & KDP Ingestion",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismissRequest, modifier = Modifier.testTag("close_sheet_button")) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Firebase Auto-Sync Preferences Indicator
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).testTag("firebase_sync_status_card")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "FIREBASE MULTI-DEVICE PREFERENCES AUTO-SYNC",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = "Export Settings & Readiness Checklist synced via WorkManager background worker",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.triggerManualCloudSync() },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary.copy(alpha = 0.2f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            elevation = null,
                            modifier = Modifier.testTag("manual_firebase_sync_button")
                        ) {
                            if (isSyncingCloud) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = GoldPrimary)
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sync Now", fontSize = 10.sp, color = GoldPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
            }

            // Section 1: Quick Configuration Mode Toggle (Kindle vs Paperback)
            item {
                Text(
                    text = "1. MANUSCRIPT FORMAT CONFIGURATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Paperback Card
                        FormatPresetCard(
                            title = "Paperback",
                            subtitle = "6\"×9\" Print",
                            badge = "PDF/DOCX",
                            isSelected = previewMode == PreviewMode.PAPERBACK_6X9,
                            icon = Icons.Default.Description,
                            details = listOf("1.0\" Margins", "Georgia 12pt", "Headers & Page #s"),
                            onSelect = { viewModel.applyPaperbackPreset() },
                            modifier = Modifier.weight(1f).testTag("select_paperback_preset")
                        )

                        // Kindle Card
                        FormatPresetCard(
                            title = "Kindle eBook",
                            subtitle = "Reflow ePub",
                            badge = "Kindle",
                            isSelected = previewMode == PreviewMode.KINDLE_EBOOK,
                            icon = Icons.Default.PhoneAndroid,
                            details = listOf("0.5\" Margins", "Georgia 14pt", "Reflowable"),
                            onSelect = { viewModel.applyKindlePreset() },
                            modifier = Modifier.weight(1f).testTag("select_kindle_preset")
                        )

                        // Web Edition Card
                        FormatPresetCard(
                            title = "Web Edition",
                            subtitle = "HTML5 Reader",
                            badge = "Web",
                            isSelected = previewMode == PreviewMode.WEB_VERSION,
                            icon = Icons.Default.Language,
                            details = listOf("HTML5 Web Reader", "Interactive TOC", "Themes & Sizing"),
                            onSelect = { viewModel.applyWebVersionPreset() },
                            modifier = Modifier.weight(1f).testTag("select_web_preset")
                        )
                    }
                }
            }

            // Section 2: Data Ingestion & Conversion Hub
            item {
                Text(
                    text = "2. DATA INGESTION & KDP CONVERSION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Input Source Tabs
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = activeInputTab == "paste",
                        onClick = { activeInputTab = "paste" },
                        label = { Text("Copy / Paste", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("tab_copy_paste")
                    )
                    FilterChip(
                        selected = activeInputTab == "file",
                        onClick = { activeInputTab = "file" },
                        label = { Text("Upload File", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("tab_upload_file")
                    )
                    FilterChip(
                        selected = activeInputTab == "preset",
                        onClick = { activeInputTab = "preset" },
                        label = { Text("Sample Template", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("tab_sample_template")
                    )
                }
            }

            // Tab Content
            item {
                when (activeInputTab) {
                    "paste" -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Paste Raw Manuscript or Article Text",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )

                                OutlinedTextField(
                                    value = pasteTitle,
                                    onValueChange = { pasteTitle = it },
                                    label = { Text("Chapter / Section Title") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("paste_chapter_title_input")
                                )

                                OutlinedTextField(
                                    value = pasteSubtitle,
                                    onValueChange = { pasteSubtitle = it },
                                    label = { Text("Subtitle / Heading 2 (Optional)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("paste_chapter_subtitle_input")
                                )

                                OutlinedTextField(
                                    value = pasteTextContent,
                                    onValueChange = { pasteTextContent = it },
                                    label = { Text("Paste Raw Text Body Here...") },
                                    minLines = 4,
                                    maxLines = 8,
                                    modifier = Modifier.fillMaxWidth().testTag("paste_text_body_input")
                                )

                                Button(
                                    onClick = {
                                        if (pasteTextContent.isNotBlank()) {
                                            viewModel.importCustomData(
                                                title = pasteTitle,
                                                subtitle = pasteSubtitle,
                                                contentText = pasteTextContent
                                            )
                                            pasteTitle = ""
                                            pasteSubtitle = ""
                                            pasteTextContent = ""
                                            onDismissRequest()
                                        } else {
                                            Toast.makeText(context, "Please enter or paste content body.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                    modifier = Modifier.fillMaxWidth().testTag("convert_pasted_data_button")
                                ) {
                                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Convert & Append to KDP Manuscript")
                                }
                            }
                        }
                    }

                    "file" -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(40.dp)
                                )

                                Text(
                                    text = "Upload Manuscript Document (.txt, .md, .html)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = "Select any document from your device. The app will automatically format headers, margins, and Georgia typography for KDP 6\"×9\" standards.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Button(
                                    onClick = { filePickerLauncher.launch("*/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                    modifier = Modifier.fillMaxWidth().testTag("launch_file_picker_button")
                                ) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Browse Files & Convert")
                                }
                            }
                        }
                    }

                    "preset" -> {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Add Standard KDP Section Templates",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )

                                Text(
                                    text = "Quickly insert pre-formatted sections like Index, References, or Conclusion into your book.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )

                                Button(
                                    onClick = {
                                        viewModel.importCustomData(
                                            title = "Appendix: Research & Citations",
                                            subtitle = "Standard KDP Reference Section",
                                            contentText = "## References & Bibliography\n\n1. American Psychological Association (2020). KDP Publishing Guidelines.\n2. Journal of Circadian Biology (2025). Morning Light & Melatonin Suppression.\n3. National Library of Medicine (2024). Diaphragmatic Breathing Effects."
                                        )
                                        onDismissRequest()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                    modifier = Modifier.fillMaxWidth().testTag("add_sample_appendix_button")
                                ) {
                                    Icon(Icons.Default.LibraryAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Pre-formatted Appendix Section")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FormatPresetCard(
    title: String,
    subtitle: String,
    badge: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    details: List<String>,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) GoldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.clickable(onClick = onSelect)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) GoldPrimary else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Active",
                        tint = SuccessGreen,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Surface(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.secondary
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

            details.forEach { detail ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = detail,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
