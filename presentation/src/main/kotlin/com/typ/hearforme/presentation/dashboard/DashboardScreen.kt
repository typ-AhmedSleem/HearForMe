package com.typ.hearforme.presentation.dashboard

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastCoerceAtLeast
import androidx.compose.ui.util.fastCoerceIn
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.typ.hearforme.designsystem.R
import com.typ.hearforme.designsystem.theme.OnBackground
import com.typ.hearforme.designsystem.theme.PrimaryBlue
import com.typ.hearforme.designsystem.theme.TextSecondary
import com.typ.hearforme.domain.model.SoundEvent
import com.typ.hearforme.domain.model.SoundType
import org.koin.compose.viewmodel.koinViewModel
import org.ocpsoft.prettytime.PrettyTime
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToCommunication: () -> Unit = {},
    onSOSClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val rms by viewModel.rms.collectAsStateWithLifecycle()
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
                    .weight(1f)
                    .animateContentSize(),
                contentAlignment = Alignment.Center
            ) {
                SoundVisualizer(
                    state = uiState,
                    rms = rms,
                    onClick = {
                        when (uiState) {
                            is DashboardUiState.MicAccessRequired -> viewModel.triggerPermissionRequest()
                            is DashboardUiState.ServiceOffline -> viewModel.enableDetection()
                            is DashboardUiState.Identified -> {
                                if ((uiState as DashboardUiState.Identified).event.type is SoundType.SomeoneSpeaking) {
                                    viewModel.disableDetection()
                                    onNavigateToCommunication()
                                }
                            }
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
                            stringResource(R.string.dashboard_most_recent),
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = {
                            viewModel.performHapticFeedback()
                            onNavigateToHistory()
                        }) {
                            Text(stringResource(R.string.see_history), fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(
                            items = history.take(1),
                            key = { it.timestamp })
                        { event ->
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
                stringResource(R.string.app_name),
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
                        if (isLive) stringResource(R.string.status_live) else stringResource(R.string.status_offline),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                }
            }

//            Spacer(modifier = Modifier.width(12.dp))

            /*IconButton(onClick = onSOSClick) {
                Text("🚨", fontSize = 24.sp)
            }*/
        }
    }
}

@Composable
fun SoundVisualizer(
    state: DashboardUiState,
    rms: Float,
    onClick: () -> Unit,
) {
    val primaryColor = if (state is DashboardUiState.Identified) {
        Color(state.event.type.color)
    } else MaterialTheme.colorScheme.primary

    val pulseScale by animateFloatAsState(
        targetValue = when (state) {
            is DashboardUiState.Identified -> 1.6f
            is DashboardUiState.ServiceOffline, is DashboardUiState.MicAccessRequired -> 0.001f
            else -> (rms * 10f).fastCoerceIn(1f, 1.5f)
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "Pulse"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .drawBehind {
                    val radius = (size.minDimension / 2.5f) * pulseScale
                    val endingRadius = (radius * 1.25f).fastCoerceAtLeast(radius)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.6f),
                                primaryColor.copy(alpha = 0.4f),
                                primaryColor.copy(alpha = 0.2f),
                                primaryColor.copy(alpha = 0.1f),
                                Color.Transparent,
                            ),
                            center = center,
                            radius = endingRadius
                        ),
                        radius = endingRadius,
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
                        transitionSpec = { (fadeIn() + scaleIn()) togetherWith (fadeOut() + scaleOut()) },
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

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedContent(
            targetState = state,
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
            transitionSpec = { fadeIn() + expandVertically() togetherWith fadeOut() + shrinkVertically() },
        ) { currentState ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (currentState) {
                    DashboardUiState.MicAccessRequired -> {
                        Text(stringResource(R.string.dashboard_mic_required), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.dashboard_grant_permission), color = TextSecondary, fontSize = 14.sp)
                    }

                    DashboardUiState.ServiceOffline -> {
                        Text(stringResource(R.string.dashboard_detection_offline), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.dashboard_start_listening), color = TextSecondary, fontSize = 14.sp)
                    }

                    DashboardUiState.Identifying -> {
//                StatusBadge(color = Color(0xFFFFB703), text = "Analyzing Sounds...")
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.dashboard_listening),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    is DashboardUiState.Identified -> {
                        val event = currentState.event
                        Spacer(modifier = Modifier.height(16.dp))
                        StatusBadge(
                            color = Color(event.type.color),
                            text = stringResource(R.string.dashboard_confidence_percentage, (event.confidence * 100).toInt())
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (event.type.nameRes != 0) stringResource(event.type.nameRes) else (event.type as? SoundType.Generic)?.displayName
                                ?: "",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        if (event.type is SoundType.SomeoneSpeaking) {
                            Text(
                                stringResource(R.string.dashboard_tap_to_see),
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
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
fun LazyItemScope.SoundHistoryItem(event: SoundEvent) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateItem(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                Text(
                    text = if (event.type.nameRes != 0) stringResource(event.type.nameRes) else (event.type as? SoundType.Generic)?.displayName ?: "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(stringResource(R.string.dashboard_confidence, (event.confidence * 100).toInt()), color = TextSecondary, fontSize = 14.sp)
            }

            val prettyTime = remember { PrettyTime(Locale.getDefault()) }
            val relativeTime = remember(event.timestamp) { prettyTime.format(Date(event.timestamp)) }

            Text(
                text = relativeTime,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun DashboardBottomBar(
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onCommunicationClick: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .height(72.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(
                        horizontal = 16.dp
                    ),
                horizontalArrangement = Arrangement.spacedBy(
                    alignment = Alignment.CenterHorizontally,
                    space = 32.dp,
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onCommunicationClick,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.size(50.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "💬",
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.surface
                        )
                    }
                }

                IconButton(
                    onClick = onHistoryClick,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.size(50.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(
                        "🕓",
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.surface
                    )
                }

                IconButton(
                    shape = RoundedCornerShape(24.dp),
                    onClick = onSettingsClick,
                    modifier = Modifier.size(50.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        contentDescription = null,
                        imageVector = Icons.Default.Settings,
                        tint = MaterialTheme.colorScheme.surface
                    )
                }
            }
        }
    }
}
