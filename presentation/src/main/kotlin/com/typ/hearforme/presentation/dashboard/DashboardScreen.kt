package com.typ.hearforme.presentation.dashboard

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.typ.hearforme.designsystem.theme.OnBackground
import com.typ.hearforme.designsystem.theme.PrimaryBlue
import com.typ.hearforme.designsystem.theme.TextSecondary
import com.typ.hearforme.domain.model.SoundEvent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToCommunication: () -> Unit = {},
    onSOSClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    val history by viewModel.history.collectAsState()
    val rms by viewModel.rms.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.onPermissionResult(granted)

        viewModel.requestPermissionTrigger.collect {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        topBar = {
            DashboardTopBar(
                isLive = uiState !is DashboardUiState.ServiceOffline && uiState !is DashboardUiState.MicAccessRequired,
                onSettingsClick = {
                    viewModel.performHapticFeedback()
                    onNavigateToSettings()
                },
                onSOSClick = {
                    viewModel.performHapticFeedback()
                    onSOSClick()
                }
            )
        },
        bottomBar = {
            DashboardBottomBar(
                onSettingsClick = {
                    viewModel.performHapticFeedback()
                    onNavigateToSettings()
                },
                onHistoryClick = {
                    viewModel.performHapticFeedback()
                    onNavigateToHistory()
                },
                onCommunicationClick = {
                    viewModel.performHapticFeedback()
                    onNavigateToCommunication()
                }
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (history.isEmpty()) 1.5f else 1f)
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center
            ) {
                SoundVisualizer(
                    state = uiState,
                    rms = rms,
                    onClick = {
                        when (uiState) {
                            is DashboardUiState.MicAccessRequired -> viewModel.triggerPermissionRequest()
                            is DashboardUiState.ServiceOffline -> viewModel.enableDetection()
                            else -> viewModel.performHapticFeedback()
                        }
                    }
                )
            }

            AnimatedVisibility(
                visible = history.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column {
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
                        TextButton(onClick = {
                            viewModel.performHapticFeedback()
                            onNavigateToHistory()
                        }) {
                            Text("See History", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(history, key = { it.timestamp }) { event ->
                            SoundHistoryItem(event)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardTopBar(
    isLive: Boolean,
    onSettingsClick: () -> Unit,
    onSOSClick: () -> Unit,
) {
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
            val indicatorColor by animateColorAsState(
                if (isLive) Color(0xFF4CAF50) else Color.LightGray,
                label = "StatusColor"
            )
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
                            .background(indicatorColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (isLive) "LIVE" else "OFFLINE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(onClick = onSOSClick) {
                Text("🚨", fontSize = 24.sp)
            }
        }
    }
}

@Composable
fun SoundVisualizer(
    state: DashboardUiState,
    rms: Float,
    onClick: () -> Unit,
) {
    val pulseScale by animateFloatAsState(
        targetValue = 1f + (rms * 4f).coerceAtMost(0.6f),
        animationSpec = tween(150, easing = LinearEasing),
        label = "Pulse"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .drawBehind {
                    val radius = (size.minDimension / 2.2f) * pulseScale
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PrimaryBlue.copy(alpha = 0.4f),
                                PrimaryBlue.copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = radius * 1.25f
                        ),
                        radius = radius * 1.25f,
                        center = center
                    )
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(220.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AnimatedContent(
                        targetState = state,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "VisualizerContent"
                    ) { targetState ->
                        when (targetState) {
                            DashboardUiState.MicAccessRequired -> Text("🎤", fontSize = 80.sp, color = Color.LightGray)
                            DashboardUiState.ServiceOffline -> Text("💤", fontSize = 80.sp, color = Color.LightGray)
                            DashboardUiState.Identifying -> Text("?", fontSize = 80.sp, color = Color(0xFFBDC3C7))
                            is DashboardUiState.Identified -> Text(targetState.event.type.emoji, fontSize = 80.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        when (state) {
            DashboardUiState.MicAccessRequired -> {
                Text("Microphone Required", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tap to Grant Permission", color = TextSecondary, fontSize = 14.sp)
            }
            DashboardUiState.ServiceOffline -> {
                Text("Detection Offline", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Tap to Start Listening", color = TextSecondary, fontSize = 14.sp)
            }
            DashboardUiState.Identifying -> {
                StatusBadge(color = Color(0xFFFFB703), text = "Analyzing Sounds...")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Listening...", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            }
            is DashboardUiState.Identified -> {
                val event = state.event
                StatusBadge(
                    color = Color(event.type.color),
                    text = "${(event.confidence * 100).toInt()}% Match"
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    event.type.displayName,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun StatusBadge(color: Color, text: String) {
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
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
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
                    .background(Color(event.type.color).copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(event.type.emoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(event.type.displayName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("${(event.confidence * 100).toInt()}% Confidence", color = TextSecondary, fontSize = 14.sp)
            }

            Text("Now", color = TextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
fun DashboardBottomBar(
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onCommunicationClick: () -> Unit,
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
            Surface(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(16.dp),
                color = OnBackground,
                onClick = onCommunicationClick
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("💬", fontSize = 20.sp)
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
