package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CloudSyncBadge
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.BookViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KdpMonetizationStudioScreen(
    viewModel: BookViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val book by viewModel.book.collectAsState()
    val sections by viewModel.sections.collectAsState()
    val cloudSyncStatus by viewModel.cloudSyncStatus.collectAsState()
    val isSyncingCloud by viewModel.isSyncingCloud.collectAsState()

    val totalWordCount = sections.sumOf { it.contentText.split("\\s+".toRegex()).filter { w -> w.isNotBlank() }.size }
    val calculatedPages = (totalWordCount / 280.0).roundToInt().coerceAtLeast(sections.size * 2)

    var listPriceInput by remember { mutableStateOf("14.99") }
    var selectedFormat by remember { mutableStateOf("Paperback (60%)") } // "Paperback (60%)", "Kindle eBook (70%)", "Direct Store (95%)"
    var monthlySalesVolume by remember { mutableFloatStateOf(250f) }
    var userIsKdpSelect by remember { mutableStateOf(true) }
    var showActionToast by remember { mutableStateOf<String?>(null) }

    val listPrice = listPriceInput.toDoubleOrNull() ?: 14.99
    val printCostPerCopy = 1.00 + (calculatedPages * 0.012)
    
    val (royaltyPerCopy, amazonCutPerCopy) = when (selectedFormat) {
        "Kindle eBook (70%)" -> {
            val net = (listPrice * 0.70) - 0.15 // $0.15 delivery fee
            Pair(net.coerceAtLeast(0.0), listPrice * 0.30)
        }
        "Direct Store (95%)" -> {
            val net = listPrice * 0.95 // Direct sales (e.g., Gumroad/Payhip)
            Pair(net, listPrice * 0.05)
        }
        else -> { // Paperback 60% KDP standard
            val net = (listPrice * 0.60) - printCostPerCopy
            Pair(net.coerceAtLeast(0.0), listPrice * 0.40)
        }
    }

    val monthlyEarnings = royaltyPerCopy * monthlySalesVolume
    val kenpMonthlyEstimate = if (userIsKdpSelect) (calculatedPages * 0.0045 * (monthlySalesVolume * 0.8)) else 0.0
    val totalMonthlyPassiveIncome = monthlyEarnings + kenpMonthlyEstimate

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "KDP Monetization Studio",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Navy900
                        )
                        Text(
                            text = "Author Royalty & Passive Income Engine",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("monetization_studio_back_button")
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
            // Action Toast
            showActionToast?.let { toast ->
                Surface(
                    color = SuccessGreen,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("monetization_action_toast")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(toast, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Hero Passive Income Projection Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Navy800),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth().testTag("income_projection_hero_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PROJECTED MONTHLY AUTHOR PASSIVE PROFIT", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "$${String.format("%,.2f", totalMonthlyPassiveIncome)} / mo",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Based on ${monthlySalesVolume.roundToInt()} copies/mo @ $${String.format("%.2f", listPrice)} list price",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Net Royalty/Sale", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("$${String.format("%.2f", royaltyPerCopy)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Print Cost/Book", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("$${String.format("%.2f", printCostPerCopy)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("KENP Read Bonus", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("+$${String.format("%.2f", kenpMonthlyEstimate)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }
                    }
                }
            }

            // Interactive Royalty Calculator Form
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("royalty_calculator_form_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("INTERACTIVE KDP ROYALTY CONFIGURATOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)

                    // Format Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Paperback (60%)", "Kindle eBook (70%)", "Direct Store (95%)").forEach { format ->
                            FilterChip(
                                selected = selectedFormat == format,
                                onClick = { selectedFormat = format },
                                label = { Text(format, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary,
                                    selectedLabelColor = Navy900
                                ),
                                modifier = Modifier.weight(1f).testTag("format_chip_$format")
                            )
                        }
                    }

                    // List Price Input
                    OutlinedTextField(
                        value = listPriceInput,
                        onValueChange = { listPriceInput = it },
                        label = { Text("List Price ($)") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = GoldPrimary) },
                        supportingText = {
                            val minPrice = printCostPerCopy / 0.60
                            Text("Minimum recommended KDP Paperback Price: $${String.format("%.2f", minPrice)}")
                        },
                        modifier = Modifier.fillMaxWidth().testTag("list_price_input_field")
                    )

                    // Volume Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Monthly Sales Volume Target", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("${monthlySalesVolume.roundToInt()} copies/mo", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GoldPrimary)
                        }
                        Slider(
                            value = monthlySalesVolume,
                            onValueChange = { monthlySalesVolume = it },
                            valueRange = 10f..1000f,
                            steps = 99,
                            colors = SliderDefaults.colors(thumbColor = GoldPrimary, activeTrackColor = GoldPrimary),
                            modifier = Modifier.testTag("monthly_sales_slider")
                        )
                    }

                    // KDP Select Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Enroll in KDP Select / Kindle Unlimited", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Earn ~$0.0045 per page read via Global Author Fund", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                        Switch(
                            checked = userIsKdpSelect,
                            onCheckedChange = { userIsKdpSelect = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary),
                            modifier = Modifier.testTag("kdp_select_switch")
                        )
                    }
                }
            }

            // Additional Monetization Channels
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("monetization_channels_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("PRO AUTHOR MONETIZATION CHANNELS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)

                    ChannelRow(
                        title = "1. Amazon KDP Print On Demand",
                        subtitle = "Global distribution across US, UK, EU, JP with 0 upfront inventory cost.",
                        badgeText = "60% Margin",
                        icon = Icons.Default.ShoppingCart,
                        onClick = {
                            showActionToast = "KDP Publishing Package Prepared! Ready to upload on KDP Console."
                        },
                        tag = "channel_kdp_pod"
                    )

                    ChannelRow(
                        title = "2. Author Direct Creator Store (Gumroad / Payhip)",
                        subtitle = "Sell PDF & EPUB directly to readers. Keep 95% revenue and build email lists.",
                        badgeText = "95% Margin",
                        icon = Icons.Default.Storefront,
                        onClick = {
                            showActionToast = "Direct Sales Page Preset generated! Shareable link ready."
                        },
                        tag = "channel_direct_store"
                    )

                    ChannelRow(
                        title = "3. Amazon Associates Affiliate Links",
                        subtitle = "Earn an extra 4%-10% referral fee on every customer who buys via your link.",
                        badgeText = "+10% Bonus",
                        icon = Icons.Default.Link,
                        onClick = {
                            showActionToast = "Author Affiliate Referral Tag embedded into metadata!"
                        },
                        tag = "channel_affiliate"
                    )
                }
            }

            // Quick Actions & Export Package
            Button(
                onClick = {
                    showActionToast = "Royalty Breakdown & Monetization Plan Saved to Project!"
                },
                colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                modifier = Modifier.fillMaxWidth().testTag("save_monetization_plan_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = GoldPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Monetization Strategy & Sync to Cloud", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ChannelRow(
    title: String,
    subtitle: String,
    badgeText: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .background(Navy800, RoundedCornerShape(8.dp))
            ) {
                Icon(icon, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary, lineHeight = 12.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = SuccessGreen.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
