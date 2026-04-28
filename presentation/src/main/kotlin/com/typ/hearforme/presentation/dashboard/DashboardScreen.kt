package com.typ.hearforme.presentation.dashboard

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastCoerceIn
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.typ.hearforme.designsystem.R
import com.typ.hearforme.designsystem.theme.OnBackground
import com.typ.hearforme.designsystem.theme.TextSecondary
import com.typ.hearforme.domain.interceptors.prays.PrayTimesInterceptor
import com.typ.hearforme.domain.model.SoundEvent
import com.typ.hearforme.domain.model.SoundType
import com.typ.hearforme.presentation.components.geminiLikeAudioWaveBackground
import com.typ.hearforme.presentation.preview.PreviewContainer
import com.typ.islamictkt.prays.enums.PrayType.ASR
import com.typ.islamictkt.prays.enums.PrayType.DHUHR
import com.typ.islamictkt.prays.enums.PrayType.FAJR
import com.typ.islamictkt.prays.enums.PrayType.ISHA
import com.typ.islamictkt.prays.enums.PrayType.MAGHRIB
import com.typ.islamictkt.prays.enums.PrayType.SUNRISE
import com.typ.islamictkt.prays.models.Pray
import kotlinx.coroutines.delay
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
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val rms by viewModel.rms.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val nextPray by remember(uiState) {
        derivedStateOf {
            val state = uiState
            if (state is DashboardUiState.Identifying) {
                state.nextPray
            } else null
        }
    }

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
    LaunchedEffect(Unit) {
        lifecycle.currentStateFlow.collect {
            if (it == Lifecycle.State.RESUMED) {
                viewModel.trackNextPrayTime()
            }
        }
    }

    Scaffold(
        topBar = {
            DashboardTopBar(
                isLive = uiState !is DashboardUiState.ServiceOffline && uiState !is DashboardUiState.MicAccessRequired,
                onHistoryClick = {
                    viewModel.performHapticFeedback()
                    onNavigateToHistory()
                },
                onSettingsClick = {
                    viewModel.performHapticFeedback()
                    onNavigateToSettings()
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPaddings ->
        val animatedRms by animateFloatAsState(
            targetValue = rms,
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
            label = "RmsAnimation"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .geminiLikeAudioWaveBackground(
                    rmsValue = animatedRms,
                    backgroundColor = MaterialTheme.colorScheme.background
                )
                .padding(innerPaddings)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PrayTimeCountdown(nextPray)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 24.dp)
                        .animateContentSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DashboardStateTextAndContent(
                        state = uiState,
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

                /*AnimatedVisibility(
                    visible = history.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
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
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = {
                                viewModel.performHapticFeedback()
                                onNavigateToHistory()
                            }) {
                                Text(stringResource(R.string.see_history), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
                }*/
            }
        }
    }
}

@Composable
fun DashboardTopBar(
    isLive: Boolean,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
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
            /*Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(PrimaryBlue, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("👂", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))*/
            Text(
                stringResource(R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            /*Surface(
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
            }*/

            Spacer(modifier = Modifier.width(12.dp))

            IconButton(onClick = onHistoryClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    contentDescription = null,
                    imageVector = Icons.Default.History,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(onClick = onSettingsClick, modifier = Modifier.size(36.dp)) {
                Icon(
                    contentDescription = null,
                    imageVector = Icons.Default.Settings,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun DashboardStateTextAndContent(
    state: DashboardUiState,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = { fadeIn() + expandVertically() togetherWith fadeOut() + shrinkVertically() },
            label = "StateText"
        ) { currentState ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (currentState) {
                    DashboardUiState.MicAccessRequired -> {
                        Text("🎤", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.dashboard_mic_required), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.dashboard_grant_permission),
                            color = TextSecondary,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    DashboardUiState.ServiceOffline -> {
                        Text("💤", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(stringResource(R.string.dashboard_detection_offline), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(R.string.dashboard_start_listening),
                            color = TextSecondary,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    is DashboardUiState.Identifying -> {
                        Text(
                            stringResource(R.string.dashboard_listening),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    is DashboardUiState.Identified -> {
                        val event = currentState.event
                        Text(event.type.emoji, fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        StatusBadge(
                            color = Color(event.type.color),
                            text = stringResource(
                                R.string.dashboard_confidence_percentage,
                                event.confidence
                                    .fastCoerceIn(0f, 0.95f)
                                    .times(100)
                                    .toInt()
                            )
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
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }

        if (state is DashboardUiState.Identifying && state.possibleSounds.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.dashboard_possible_sounds),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                    )

                    state.possibleSounds.forEach { event ->
                        val soundName =
                            if (event.type.nameRes != 0) stringResource(event.type.nameRes) else (event.type as? SoundType.Generic)?.displayName ?: ""
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = { Text(soundName, fontWeight = FontWeight.SemiBold) },
                            leadingContent = { Text(event.type.emoji, fontSize = 24.sp) },
                            trailingContent = {
                                Text(
                                    "${(event.confidence * 100).toInt()}%",
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        )
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
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        val type = event.type
        val eventName = if (type is SoundType.PrayTime) {
            stringResource(R.string.sound_type_pray_times)
        } else {
            if (type.nameRes != 0) {
                stringResource(type.nameRes)
            } else {
                (type as? SoundType.Generic)?.displayName ?: ""
            }
        }

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
                    text = eventName,
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
private fun PrayTimeCountdown(nextPray: Pray?) {
    if (nextPray == null) return

    var remainMillis by remember(nextPray) { mutableLongStateOf(nextPray.time.toMillis() - System.currentTimeMillis()) }

    LaunchedEffect(nextPray) {
        while (true) {
            remainMillis = nextPray.time.toMillis() - System.currentTimeMillis()
            delay(1000)
        }
    }

    if (remainMillis > 0) {
        val h = remainMillis / 3600000
        val m = (remainMillis % 3600000) / 60000
        val s = (remainMillis % 60000) / 1000
        val timeString = String.format(
            locale = Locale.getDefault(),
            format = "%02d:%02d:%02d",
            h, m, s
        )

        Surface(
            modifier = Modifier.padding(top = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF009688).copy(alpha = 0.1f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🕌", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(
                        when (nextPray.type) {
                            FAJR -> R.string.fajr
                            SUNRISE -> R.string.sunrise
                            DHUHR -> R.string.dhuhr
                            ASR -> R.string.asr
                            MAGHRIB -> R.string.maghrib
                            ISHA -> R.string.isha
                        }
                    ) + " ${stringResource(R.string.suffix_in)} " + timeString,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF009688),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview
@Composable
private fun PrayTimeCountdownPreview() {
    val prays = remember { PrayTimesInterceptor().todayPrays }
    val nextPray = prays.nextPray
    PreviewContainer {
        PrayTimeCountdown(nextPray)
    }
}