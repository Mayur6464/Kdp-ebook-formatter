package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.formatter.IssueSeverity
import com.example.formatter.ReadabilityIssue
import com.example.formatter.TextAnalysisResult
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KdpTextAnalysisBottomSheet(
    analysisResult: TextAnalysisResult?,
    isAnalyzing: Boolean,
    onDismissRequest: () -> Unit,
    onApplyFix: (original: String, replacement: String) -> Unit,
    onReAnalyze: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var selectedFilter by remember { mutableStateOf<IssueSeverity?>(null) } // null = All
    var copiedIssueId by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.testTag("kdp_text_analysis_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Gemini KDP Text & Readability Analyzer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Navy900
                        )
                    }
                    Text(
                        text = if (analysisResult?.analyzedByGemini == true) "Powered by Gemini 3.5 Flash AI" else "KDP Editorial & Readability Rules",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Row {
                    IconButton(
                        onClick = onReAnalyze,
                        enabled = !isAnalyzing,
                        modifier = Modifier.testTag("reanalyze_gemini_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Re-analyze", tint = Navy900)
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.testTag("close_analysis_sheet_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Navy900)
                    }
                }
            }

            if (isAnalyzing) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = GoldPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Evaluating passive voice, run-ons & KDP readability with Gemini...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (analysisResult != null) {
                // Readability Score Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = Navy800),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("readability_score_card")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("READABILITY HEALTH SCORE", color = GoldPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${analysisResult.readabilityScore} / 100", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("Grade Level: ${analysisResult.gradeLevel}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        }

                        // Summary Badges
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            BadgeItem(count = analysisResult.passiveVoiceCount, label = "Passive Voice", color = Color(0xFFEF4444))
                            BadgeItem(count = analysisResult.runOnSentenceCount, label = "Run-on Sentences", color = Color(0xFFF59E0B))
                            BadgeItem(count = analysisResult.kdpPitfallCount, label = "KDP Pitfalls", color = Color(0xFF3B82F6))
                        }
                    }
                }

                // Filter Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("All (${analysisResult.issues.size})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldPrimary, selectedLabelColor = Navy900),
                        modifier = Modifier.testTag("filter_all_issues_chip")
                    )
                    FilterChip(
                        selected = selectedFilter == IssueSeverity.PASSIVE_VOICE,
                        onClick = { selectedFilter = IssueSeverity.PASSIVE_VOICE },
                        label = { Text("Passive (${analysisResult.passiveVoiceCount})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFEF4444), selectedLabelColor = Color.White),
                        modifier = Modifier.testTag("filter_passive_chip")
                    )
                    FilterChip(
                        selected = selectedFilter == IssueSeverity.RUN_ON_SENTENCE,
                        onClick = { selectedFilter = IssueSeverity.RUN_ON_SENTENCE },
                        label = { Text("Run-ons (${analysisResult.runOnSentenceCount})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFF59E0B), selectedLabelColor = Navy900),
                        modifier = Modifier.testTag("filter_runon_chip")
                    )
                    FilterChip(
                        selected = selectedFilter == IssueSeverity.KDP_PITFALL,
                        onClick = { selectedFilter = IssueSeverity.KDP_PITFALL },
                        label = { Text("Pitfalls (${analysisResult.kdpPitfallCount})", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF3B82F6), selectedLabelColor = Color.White),
                        modifier = Modifier.testTag("filter_pitfall_chip")
                    )
                }

                val filteredIssues = remember(selectedFilter, analysisResult) {
                    if (selectedFilter == null) analysisResult.issues
                    else analysisResult.issues.filter { it.type == selectedFilter }
                }

                if (filteredIssues.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No issues detected in this category!", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Your chapter text meets high KDP publication standards.", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(filteredIssues, key = { it.id }) { issue ->
                            IssueCardItem(
                                issue = issue,
                                isCopied = copiedIssueId == issue.id,
                                onCopySuggestion = {
                                    clipboardManager.setText(AnnotatedString(issue.suggestionText))
                                    copiedIssueId = issue.id
                                },
                                onApplyFix = {
                                    if (issue.originalText.isNotBlank() && issue.suggestionText.isNotBlank()) {
                                        onApplyFix(issue.originalText, issue.suggestionText)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeItem(count: Int, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$count $label",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun IssueCardItem(
    issue: ReadabilityIssue,
    isCopied: Boolean,
    onCopySuggestion: () -> Unit,
    onApplyFix: () -> Unit
) {
    val (badgeBg, badgeText, icon) = when (issue.type) {
        IssueSeverity.PASSIVE_VOICE -> Triple(Color(0xFFFEF2F2), Color(0xFFDC2626), Icons.Default.RecordVoiceOver)
        IssueSeverity.RUN_ON_SENTENCE -> Triple(Color(0xFFFFFBEB), Color(0xFFD97706), Icons.Default.LinearScale)
        IssueSeverity.KDP_PITFALL -> Triple(Color(0xFFEFF6FF), Color(0xFF2563EB), Icons.Default.Warning)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth().testTag("issue_card_${issue.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, contentDescription = null, tint = badgeText, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = issue.type.name.replace("_", " "),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeText
                        )
                    }
                }

                Text(issue.title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Navy900)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Original Text
            Text("ORIGINAL TEXT:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
            ) {
                Text(
                    text = "\"${issue.originalText}\"",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Serif,
                    color = Color(0xFF991B1B),
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Suggestion
            Text("SUGGESTED REVISION:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
            Surface(
                color = SuccessGreen.copy(alpha = 0.1f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
            ) {
                Text(
                    text = issue.suggestionText,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    color = SuccessGreen,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = issue.explanation,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.secondary,
                lineHeight = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onCopySuggestion,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("copy_issue_suggestion_button")
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isCopied) "Copied!" else "Copy Fix", fontSize = 10.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onApplyFix,
                    colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("apply_issue_fix_button")
                ) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Apply Fix", fontSize = 10.sp, color = Color.White)
                }
            }
        }
    }
}
