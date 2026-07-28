package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SuccessGreen

@Composable
fun CloudSyncBadge(
    statusText: String,
    isSyncing: Boolean,
    onTriggerSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSyncing) GoldPrimary.copy(alpha = 0.15f) else SuccessGreen.copy(alpha = 0.12f),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onTriggerSync() }
            .testTag("cloud_sync_badge_header")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(16.dp)
                    .background(
                        color = if (isSyncing) GoldPrimary else SuccessGreen,
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (isSyncing) Icons.Default.CloudSync else Icons.Default.CloudDone,
                    contentDescription = "Cloud Sync Status",
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = statusText,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSyncing) GoldPrimary else SuccessGreen
            )
        }
    }
}
