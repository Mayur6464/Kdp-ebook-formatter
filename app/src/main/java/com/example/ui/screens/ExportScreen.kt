package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.formatter.KdpDocxExporter
import com.example.ui.components.ExportSettingsBottomSheet
import com.example.ui.components.FontDiagnosticCard
import com.example.ui.components.PdfDiagnosticCard
import com.example.ui.components.PdfGenerationProgressDialog
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    viewModel: BookViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val book by viewModel.book.collectAsState()
    val sections by viewModel.sections.collectAsState()
    val exportSuccessMessage by viewModel.exportSuccessMessage.collectAsState()
    val pdfDiagnosticReport by viewModel.pdfDiagnosticReport.collectAsState()
    val fontDiagnosticReport by viewModel.fontDiagnosticReport.collectAsState()

    val currentBook = book ?: return

    var showExportSettingsSheet by remember { mutableStateOf(false) }

    LaunchedEffect(exportSuccessMessage) {
        exportSuccessMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export & Final Delivery", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("export_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showExportSettingsSheet = true },
                        modifier = Modifier.testTag("open_export_settings_top_button")
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Export Settings & Ingestion", tint = GoldPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
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
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GoldPrimary.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Seamless KDP Delivery Hub", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("All files are formatted strictly according to Amazon KDP 6\"×9\" paperback & Kindle specifications.", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showExportSettingsSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            modifier = Modifier.fillMaxWidth().testTag("open_export_settings_banner_button")
                        ) {
                            Icon(Icons.Default.SettingsSuggest, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Export Settings & Import Data Sheet", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Diagnostic Report Section
            pdfDiagnosticReport?.let { report ->
                item {
                    PdfDiagnosticCard(
                        report = report,
                        onFixDiagnostic = { diagnosticId ->
                            viewModel.fixPdfDiagnostic(diagnosticId)
                        },
                        modifier = Modifier.testTag("pdf_diagnostic_card")
                    )
                }
            }

            fontDiagnosticReport?.let { fontReport ->
                item {
                    FontDiagnosticCard(
                        report = fontReport,
                        onFixFontIssue = { fontIssueId ->
                            viewModel.fixFontDiagnosticIssue(fontIssueId)
                        },
                        modifier = Modifier.testTag("font_diagnostic_card")
                    )
                }
            }

            item {
                Text(
                    text = "SELECT DELIVERABLE FORMAT",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 1.sp
                )
            }

            // Deliverables Options
            item {
                ExportOptionCard(
                    title = "KDP Print (.docx / HTML)",
                    subtitle = "Formatted 6\"×9\" paperback layout ready for Amazon KDP upload",
                    badge = "Paperback",
                    icon = Icons.Default.Description,
                    buttonTag = "export_kdp_print_button",
                    onClick = {
                        viewModel.generatePdfWithProgress(context, "KDP_PRINT_DOCX") { exportedFile ->
                            shareFile(context, exportedFile, "text/html")
                        }
                    }
                )
            }

            item {
                ExportOptionCard(
                    title = "Kindle eBook (.docx / HTML)",
                    subtitle = "Reflowable eBook layout optimized for Amazon Kindle Direct Publishing",
                    badge = "Kindle",
                    icon = Icons.Default.PhoneAndroid,
                    buttonTag = "export_kindle_button",
                    onClick = {
                        viewModel.generatePdfWithProgress(context, "KINDLE_DOCX") { exportedFile ->
                            shareFile(context, exportedFile, "text/html")
                        }
                    }
                )
            }

            item {
                ExportOptionCard(
                    title = "Interactive Web Edition (.html / Embed)",
                    subtitle = "Standalone HTML5 Web Cloud Reader with light/dark/sepia theme toggles & web embed code",
                    badge = "Web Reader",
                    icon = Icons.Default.Language,
                    buttonTag = "export_web_button",
                    onClick = {
                        viewModel.generatePdfWithProgress(context, "WEB_VERSION_HTML") { exportedFile ->
                            shareFile(context, exportedFile, "text/html")
                        }
                    }
                )
            }

            item {
                ExportOptionCard(
                    title = "Google Docs Seamless Delivery",
                    subtitle = "Copy structured manuscript text for seamless 1-tap paste into Google Docs",
                    badge = "Google Docs",
                    icon = Icons.Default.ContentCopy,
                    buttonTag = "export_gdocs_button",
                    onClick = {
                        val pasteText = KdpDocxExporter.generateGoogleDocsPasteText(currentBook, sections)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("KDP Google Docs Manuscript", pasteText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied manuscript to clipboard for Google Docs!", Toast.LENGTH_LONG).show()
                    }
                )
            }

            item {
                ExportOptionCard(
                    title = "PDF Proof Copy",
                    subtitle = "Generate print proof copy preview with multi-stage progress tracking",
                    badge = "PDF Print",
                    icon = Icons.Default.PictureAsPdf,
                    buttonTag = "export_pdf_button",
                    onClick = {
                        viewModel.generatePdfWithProgress(context, "PDF_PRINT") { exportedFile ->
                            shareFile(context, exportedFile, "application/pdf")
                        }
                    }
                )
            }

            // KDP Upload Instructions
            item {
                Text(
                    text = "AMAZON KDP UPLOAD STEPS",
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
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StepItem("1", "Log in to Amazon KDP (kdp.amazon.com) & select 'Create Paperback'.")
                        StepItem("2", "Set Trim Size to 6\" × 9\" and Bleed to 'No Bleed'.")
                        StepItem("3", "Upload the exported KDP Print (.docx/HTML) or PDF file.")
                        StepItem("4", "Preview in KDP Print Previewer & launch publication!")
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

        // PDF Animated Progress Dialog during PDF compilation
        PdfGenerationProgressDialog(viewModel = viewModel)
    }
}

@Composable
fun ExportOptionCard(
    title: String,
    subtitle: String,
    badge: String,
    icon: ImageVector,
    buttonTag: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = GoldPrimary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(color = GoldPrimary.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                        Text(badge, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GoldPrimary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                modifier = Modifier.testTag(buttonTag)
            ) {
                Text("Export", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun StepItem(num: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(color = GoldPrimary, shape = RoundedCornerShape(12.dp), modifier = Modifier.size(20.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Text(num, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(text, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun shareFile(context: Context, file: java.io.File, mimeType: String) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export Manuscript File"))
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Exported file saved to: ${file.name}", Toast.LENGTH_LONG).show()
    }
}
