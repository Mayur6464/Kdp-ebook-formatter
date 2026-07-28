package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.Navy900
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.BookViewModel

@Composable
fun PdfGenerationProgressDialog(
    viewModel: BookViewModel,
    onDismissRequest: () -> Unit = {}
) {
    val isGeneratingPdf by viewModel.isGeneratingPdf.collectAsState()
    val progress by viewModel.pdfGenerationProgress.collectAsState()
    val statusText by viewModel.pdfGenerationStatusText.collectAsState()
    val currentStep by viewModel.pdfGenerationCurrentStep.collectAsState()
    val totalSteps by viewModel.pdfGenerationTotalSteps.collectAsState()
    val book by viewModel.book.collectAsState()
    val sections by viewModel.sections.collectAsState()

    if (!isGeneratingPdf) return

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 350),
        label = "pdf_progress_animation"
    )

    val percentInt = (animatedProgress * 100).toInt().coerceIn(0, 100)

    Dialog(
        onDismissRequest = { viewModel.cancelPdfGeneration() },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Navy900),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("pdf_generation_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Icon & Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = GoldPrimary.copy(alpha = 0.2f),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = "PDF Generation",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "PDF PROOF COMPILATION",
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Step $currentStep of $totalSteps Processing",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Text(
                        text = "$percentInt%",
                        color = GoldPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        modifier = Modifier.testTag("pdf_progress_percent_text")
                    )
                }

                // Smooth Animated Linear Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .testTag("pdf_progress_bar_container")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .clip(RoundedCornerShape(5.dp))
                            .background(GoldPrimary)
                            .testTag("pdf_progress_bar")
                    )
                }

                // Step Indicators (1..4)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val stepLabels = listOf("Parse", "Geometry", "Typeset", "PDF Proof")
                    stepLabels.forEachIndexed { index, label ->
                        val stepNum = index + 1
                        val isDone = currentStep > stepNum || percentInt >= 100
                        val isCurrent = currentStep == stepNum && percentInt < 100

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        color = when {
                                            isDone -> SuccessGreen
                                            isCurrent -> GoldPrimary
                                            else -> Color.White.copy(alpha = 0.2f)
                                        },
                                        shape = CircleShape
                                    )
                            ) {
                                if (isDone) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else {
                                    Text(
                                        text = "$stepNum",
                                        color = if (isCurrent) Navy900 else Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = label,
                                fontSize = 9.sp,
                                color = if (isCurrent) GoldPrimary else Color.White.copy(alpha = 0.6f),
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Manuscript Meta Info Card
                Surface(
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = book?.title ?: "KDP Manuscript Title",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                        Text(
                            text = "${sections.size} Sections • 6\"×9\" Trim Size • ${book?.bodyFontFamily ?: "Georgia"} Vector Typography",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                // Status Message
                Surface(
                    color = GoldPrimary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusText.ifBlank { "Compiling PDF manuscript..." },
                        color = GoldPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(10.dp)
                            .testTag("pdf_status_text")
                    )
                }

                // Cancel Button
                TextButton(
                    onClick = { viewModel.cancelPdfGeneration() },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .testTag("pdf_cancel_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Cancel",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cancel Generation", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
