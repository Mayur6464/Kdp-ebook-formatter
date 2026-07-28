package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CloudSyncBadge
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.BookViewModel

data class InvitedFriend(
    val id: String,
    val name: String,
    val email: String,
    val dateJoined: String,
    val status: String, // "Signed Up", "KDP Book Published"
    val bonusEarned: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferralDashboardScreen(
    viewModel: BookViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val cloudSyncStatus by viewModel.cloudSyncStatus.collectAsState()
    val isSyncingCloud by viewModel.isSyncingCloud.collectAsState()

    var authorCode by remember { mutableStateOf("AUTHOR-ALEX-7829") }
    var isEditingCode by remember { mutableStateOf(false) }
    val inviteLink = "https://kdp.authorstudio.app/invite/$authorCode"

    var isLinkCopied by remember { mutableStateOf(false) }
    var actionToastMessage by remember { mutableStateOf<String?>(null) }

    // Dynamic referral stats state
    var totalInvitedCount by remember { mutableIntStateOf(3) }
    var totalAiCredits by remember { mutableIntStateOf(150) }
    var totalRoyaltyBonusDollars by remember { mutableDoubleStateOf(75.0) }

    val invitedFriendsList = remember {
        mutableStateListOf(
            InvitedFriend("1", "Elena Rostova", "elena.write@gmail.com", "Today at 2:15 PM", "KDP Book Published", "+$25 Cash Bonus + 50 Credits"),
            InvitedFriend("2", "Marcus Thorne", "m.thorne.books@yahoo.com", "Yesterday", "Signed Up", "+$25 Cash Bonus + 50 Credits"),
            InvitedFriend("3", "David Chen", "david.chen.author@outlook.com", "3 days ago", "Signed Up", "+$25 Cash Bonus + 50 Credits")
        )
    }

    val simulateFriendSignUp = {
        val newNames = listOf("Sophia Vance", "Liam O'Connor", "Aria Patel", "Noah Sterling", "Chloe Bennett")
        val randomName = newNames.random()
        val email = "${randomName.lowercase().replace(" ", ".")}@gmail.com"
        
        totalInvitedCount += 1
        totalAiCredits += 50
        totalRoyaltyBonusDollars += 25.0

        invitedFriendsList.add(
            0,
            InvitedFriend(
                id = java.util.UUID.randomUUID().toString(),
                name = randomName,
                email = email,
                dateJoined = "Just now",
                status = "Signed Up",
                bonusEarned = "+$25 Cash Bonus + 50 Credits"
            )
        )
        actionToastMessage = "🎉 New referral! $randomName signed up with your link. +$25 & +50 Credits added!"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Author Referral & Creator Rewards",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Navy900
                        )
                        Text(
                            text = "Invite fellow authors • Earn $25 bonus & AI credits",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("referral_dashboard_back_button")
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
            // Notification Toast
            actionToastMessage?.let { toast ->
                Surface(
                    color = SuccessGreen,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("referral_action_toast")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(toast, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        IconButton(onClick = { actionToastMessage = null }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White)
                        }
                    }
                }
            }

            // Hero Reward Balance Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Navy800),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth().testTag("referral_hero_rewards_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("TOTAL REWARDS EARNED", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Author Cash Bonus", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("$${String.format("%.2f", totalRoyaltyBonusDollars)}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }

                        Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color.White.copy(alpha = 0.2f)))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("AI Cover Credits", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("$totalAiCredits PTS", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = GoldPrimary)
                        }

                        Box(modifier = Modifier.width(1.dp).height(36.dp).background(Color.White.copy(alpha = 0.2f)))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Authors Joined", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                            Text("$totalInvitedCount", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = SuccessGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            actionToastMessage = "🎉 $${String.format("%.2f", totalRoyaltyBonusDollars)} Royalty Bonus Payout Request submitted to Amazon Direct Deposit!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("redeem_rewards_payout_button")
                    ) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Navy900)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Withdraw $${String.format("%.2f", totalRoyaltyBonusDollars)} Cash Royalty Credit", color = Navy900, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Invitation Link Generator Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("invitation_link_card")
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
                        Text("YOUR UNIQUE AUTHOR INVITATION LINK", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)

                        IconButton(
                            onClick = { isEditingCode = !isEditingCode },
                            modifier = Modifier.size(24.dp).testTag("edit_referral_code_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Code", tint = Navy900, modifier = Modifier.size(16.dp))
                        }
                    }

                    if (isEditingCode) {
                        OutlinedTextField(
                            value = authorCode,
                            onValueChange = { authorCode = it.uppercase().replace(" ", "-") },
                            label = { Text("Custom Author Tag / Code") },
                            trailingIcon = {
                                IconButton(onClick = { isEditingCode = false }) {
                                    Icon(Icons.Default.Check, contentDescription = "Save Code", tint = SuccessGreen)
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("referral_code_input_field")
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = inviteLink,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Navy900,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(inviteLink))
                                    isLinkCopied = true
                                    actionToastMessage = "Invitation link copied to clipboard!"
                                },
                                modifier = Modifier.testTag("copy_invite_link_button")
                            ) {
                                Icon(
                                    imageVector = if (isLinkCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                    contentDescription = "Copy Link",
                                    tint = GoldPrimary
                                )
                            }
                        }
                    }

                    // Share & Test Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val shareIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Hey! Join me on KDP Author Studio to format & publish Kindle books automatically. Use my link to get 50 free AI credits: $inviteLink"
                                    )
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Author Invitation Link"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Navy800),
                            modifier = Modifier.weight(1f).testTag("share_invite_link_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Link", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { simulateFriendSignUp() },
                            modifier = Modifier.weight(1f).testTag("simulate_referral_signup_button")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Navy900, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simulate Sign-up", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Reward Tiers & Unlocks
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("referral_tiers_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("AUTHOR MILESTONE REWARD TIERS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)

                    TierRowItem(
                        tierTitle = "Tier 1: 1 Author Friend",
                        perks = "50 Free AI Cover Syntheses + $25 Credit",
                        isUnlocked = totalInvitedCount >= 1,
                        currentCount = totalInvitedCount,
                        targetCount = 1,
                        tag = "tier_1_row"
                    )

                    TierRowItem(
                        tierTitle = "Tier 2: 3 Author Friends",
                        perks = "Free KDP ISBN & Barcode Generator Pro + $75 Credit",
                        isUnlocked = totalInvitedCount >= 3,
                        currentCount = totalInvitedCount,
                        targetCount = 3,
                        tag = "tier_2_row"
                    )

                    TierRowItem(
                        tierTitle = "Tier 3: 5 Author Friends",
                        perks = "Unlimited Cloud Sync + $125 Royalty Bonus",
                        isUnlocked = totalInvitedCount >= 5,
                        currentCount = totalInvitedCount,
                        targetCount = 5,
                        tag = "tier_3_row"
                    )

                    TierRowItem(
                        tierTitle = "Tier 4: 10 Author Friends",
                        perks = "Lifetime Author VIP Pass + 100% Royalty Multiplier",
                        isUnlocked = totalInvitedCount >= 10,
                        currentCount = totalInvitedCount,
                        targetCount = 10,
                        tag = "tier_4_row"
                    )
                }
            }

            // Referred Friends Activity Log
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("referred_friends_activity_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("INVITED AUTHORS ACTIVITY LOG (${invitedFriendsList.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)

                    invitedFriendsList.forEach { friend ->
                        FriendRow(friend = friend)
                    }
                }
            }
        }
    }
}

@Composable
private fun TierRowItem(
    tierTitle: String,
    perks: String,
    isUnlocked: Boolean,
    currentCount: Int,
    targetCount: Int,
    tag: String
) {
    Surface(
        color = if (isUnlocked) SuccessGreen.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = if (isUnlocked) 1.dp else 0.5.dp,
            color = if (isUnlocked) SuccessGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        modifier = Modifier.fillMaxWidth().testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .background(if (isUnlocked) SuccessGreen else MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isUnlocked) Icons.Default.Check else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isUnlocked) Color.White else Navy900,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(tierTitle, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Navy900)
                Text(perks, fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = if (isUnlocked) SuccessGreen else GoldPrimary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = if (isUnlocked) "UNLOCKED" else "$currentCount/$targetCount",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isUnlocked) Color.White else GoldPrimary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun FriendRow(friend: InvitedFriend) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().testTag("invited_friend_${friend.id}")
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Navy800, CircleShape)
                ) {
                    Text(
                        text = friend.name.take(1),
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(friend.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Navy900)
                    Text("${friend.email} • ${friend.dateJoined}", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = SuccessGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = friend.status,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
                Text(friend.bonusEarned, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = GoldPrimary)
            }
        }
    }
}
