package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.formatter.DiagnosticSeverity
import com.example.formatter.FontDiagnosticItem
import com.example.formatter.FontDiagnosticReport
import com.example.formatter.FontEmbedStatus
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.Navy900
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber

@Composable
fun FontDiagnosticCard(
    report: FontDiagnosticReport,
    onFixFontIssue: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("font_diagnostic_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = if (report.isFontCompliant) SuccessGreen.copy(alpha = 0.2f) else WarningAmber.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (report.isFontCompliant) Icons.Default.FontDownload else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (report.isFontCompliant) SuccessGreen else WarningAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Document Font & Style Diagnostic Module",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${report.healthScore}% Typography Health • ${report.safeEmbeddedCount} Embedded Safe • ${report.unsupportedCount + report.nonEmbeddedCount} Risk Fonts",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.testTag("toggle_font_diagnostics_expanded")
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand Font Diagnostics"
                    )
                }
            }

            if (isExpanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Diagnostic Items List
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    report.items.forEach { item ->
                        FontDiagnosticRowItem(
                            item = item,
                            onFix = { onFixFontIssue(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FontDiagnosticRowItem(
    item: FontDiagnosticItem,
    onFix: () -> Unit
) {
    val statusColor = when (item.severity) {
        DiagnosticSeverity.PASS -> SuccessGreen
        DiagnosticSeverity.WARNING -> WarningAmber
        DiagnosticSeverity.ERROR -> MaterialTheme.colorScheme.error
    }

    val statusBadgeText = when (item.embedStatus) {
        FontEmbedStatus.EMBEDDED_SAFE -> "100% VECTOR EMBEDDED"
        FontEmbedStatus.SYSTEM_GENERIC -> "GENERIC SYSTEM FONT"
        FontEmbedStatus.UNSUPPORTED_RISKY -> "UNSUPPORTED DISPLAY FONT"
        FontEmbedStatus.NON_EMBEDDED_REJECT_RISK -> "NON-EMBEDDED REJECT RISK"
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (item.severity == DiagnosticSeverity.PASS) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = item.styleTarget,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = statusBadgeText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Font Family: ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = item.fontName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "(${item.usageLocation})",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = item.issueDescription,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (item.severity != DiagnosticSeverity.PASS && item.isAutoFixable) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.recommendation,
                        fontSize = 10.sp,
                        color = WarningAmber,
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = onFix,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("fix_font_issue_${item.id}")
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = Navy900, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Auto-Fix Font", fontSize = 10.sp, color = Navy900, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
