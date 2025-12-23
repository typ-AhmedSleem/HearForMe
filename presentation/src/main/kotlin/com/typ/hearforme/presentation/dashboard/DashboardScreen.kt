package com.typ.hearforme.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typ.hearforme.designsystem.theme.OnBackground
import com.typ.hearforme.designsystem.theme.PrimaryBlue
import com.typ.hearforme.designsystem.theme.TextSecondary
import com.typ.hearforme.domain.model.SoundEvent
import com.typ.hearforme.domain.model.SoundType

@Composable
fun DashboardScreen(
    currentEvent: SoundEvent? = null,
    history: List<SoundEvent> = emptyList(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToCommunication: () -> Unit = {},
) {
    Scaffold(
        topBar = { DashboardTopBar(onNavigateToSettings) },
        bottomBar = {
            DashboardBottomBar(
                onSettingsClick = onNavigateToSettings,
                onHistoryClick = onNavigateToHistory,
                onCommunicationClick = onNavigateToCommunication
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPaddings ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPaddings)
                .padding(horizontal = 20.dp)
        ) {
            // Central Visualizer Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center
            ) {
                SoundVisualizer(currentEvent)
            }

            // History Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "PAST HOUR",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToHistory) {
                    Text("See History", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                }
            }

            // History List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(history) { event ->
                    SoundHistoryItem(event)
                }

                // Mock items if empty
                if (history.isEmpty()) {
                    items(2) { index ->
                        MockSoundHistoryItem(index)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardTopBar(onSettingsClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(PrimaryBlue, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("👂", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Hear for Me",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Live Indicator
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.height(32.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF4CAF50), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("LIVE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = OnBackground)
            }
        }
    }
}

@Composable
fun SoundVisualizer(event: SoundEvent?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            // Rings
            Surface(
                modifier = Modifier.size(220.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (event == null) {
                        Text("?", fontSize = 80.sp, color = Color(0xFFBDC3C7))
                    } else {
                        Text(getEmojiForType(event.type), fontSize = 80.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Badge
        Surface(
            color = OnBackground,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFFFFB703), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (event == null) "Analyzing Patterns..." else "High Match",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = event?.type?.displayName ?: "Unidentified\nSound",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
fun SoundHistoryItem(event: SoundEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFFFFF5F5), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(getEmojiForType(event.type), fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(event.type.displayName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Room • High Match", color = TextSecondary, fontSize = 14.sp)
            }

            Text("Now", color = TextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
fun MockSoundHistoryItem(index: Int) {
    val (name, emoji, color) = when (index) {
        0 -> Triple("Doorbell", "🏠", Color(0xFFFFF7ED))
        1 -> Triple("Dog Barking", "🐕", Color(0xFFEEF2FF))
        else -> Triple("Running Water", "💧", Color(0xFFECFEFF))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(color, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Living Room • Medium Match", color = TextSecondary, fontSize = 14.sp)
            }

            val time = when (index) {
                0 -> "2m"
                1 -> "15m"
                else -> "42m"
            }
            Text(time, color = TextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
fun DashboardBottomBar(
    onSettingsClick: () -> Unit = {},
    onHistoryClick: () -> Unit = {},
    onCommunicationClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        shape = RoundedCornerShape(32.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Active listening button -> Communication / Stats
            Surface(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(16.dp),
                color = OnBackground,
                onClick = onCommunicationClick
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("📊", fontSize = 20.sp)
                }
            }

            IconButton(onClick = onHistoryClick) {
                Text("🕓", fontSize = 24.sp)
            }

            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = TextSecondary)
            }
        }
    }
}

fun getEmojiForType(type: SoundType): String = when (type) {
    SoundType.BABY_CRYING -> "👶"
    SoundType.DOORBELL -> "🏠"
    SoundType.ALARM_SIREN -> "🚨"
    SoundType.DOG_BARKING -> "🐕"
    SoundType.SMOKE_ALARM -> "🔥"
    else -> "❓"
}
