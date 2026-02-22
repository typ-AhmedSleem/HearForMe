package com.typ.hearforme.presentation.communication

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typ.hearforme.designsystem.R
import com.typ.hearforme.designsystem.theme.HearForMeTheme
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunicationScreen(
    onBack: () -> Unit,
    viewModel: CommunicationViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val engineState by viewModel.engineState.collectAsState()

    CommunicationScreenContent(
        uiState = uiState,
        engineState = engineState,
        onBack = onBack,
        onStartListening = { viewModel.startListening() },
        onStopListening = { viewModel.stopListening() },
        onRetry = { viewModel.retry() },
        onClearTranscription = { viewModel.clearTranscription() }
    )

    // Auto-start listening if ready
    LaunchedEffect(Unit) {
        if (engineState is CommunicationEngineState.Ready) {
            viewModel.startListening()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunicationScreenContent(
    uiState: CommunicationUiState,
    engineState: CommunicationEngineState,
    onBack: () -> Unit,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onRetry: () -> Unit,
    onClearTranscription: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.communication_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        /*Text(
                            text = stringResource(R.string.communication_title).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )*/
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = onClearTranscription) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = stringResource(R.string.clear)
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // State Badge
            CommunicationStateBadge(state = engineState)

            // Transcription Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = engineState,
                    transitionSpec = {
                        fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                    },
                    label = "ScreenState"
                ) { state ->
                    when (state) {
                        is CommunicationEngineState.Busy -> ErrorStateView(
                            icon = Icons.Default.MicOff,
                            title = stringResource(R.string.audio_conflict),
                            message = stringResource(R.string.mic_in_use),
                            actionText = stringResource(R.string.mic_in_use).uppercase(),
                            onRetry = onRetry
                        )

                        is CommunicationEngineState.Offline -> ErrorStateView(
                            icon = Icons.Outlined.LinkOff,
                            title = stringResource(R.string.network_issue),
                            message = stringResource(R.string.connection_lost),
                            actionText = stringResource(R.string.retry_connection).uppercase(),
                            onRetry = onRetry
                        )

                        else -> TranscriptionView(
                            transcribedText = uiState.transcribedText,
                            partialTranscription = uiState.partialTranscription,
                            hint = stringResource(R.string.communication_hint)
                        )
                    }
                }
            }

            // Bottom Action Section
            BottomActionSection(
                state = engineState,
                onStart = onStartListening,
                onStop = onStopListening,
                onRetry = onRetry
            )
        }
    }
}

@Composable
fun CommunicationStateBadge(state: CommunicationEngineState) {
    val config = when (state) {
        CommunicationEngineState.Ready -> BadgeConfig(
            stringResource(R.string.mic_ready),
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.onPrimaryContainer
        )

        CommunicationEngineState.Listening -> BadgeConfig(
            stringResource(R.string.communication_listening),
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.onSecondaryContainer
        )

        CommunicationEngineState.Busy -> BadgeConfig(
            stringResource(R.string.mic_busy),
            MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.error
        )

        CommunicationEngineState.Offline -> BadgeConfig(
            stringResource(R.string.network_issue),
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.error
        )
    }

    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = config.containerColor,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(config.dotColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = config.label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = config.dotColor,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun TranscriptionView(
    transcribedText: String,
    partialTranscription: String,
    hint: String,
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(transcribedText, partialTranscription) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        if (transcribedText.isEmpty() && partialTranscription.isEmpty()) {
            Text(
                text = hint,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        } else {
            val annotatedText = buildAnnotatedString {
                // Dimmed older history
                val words = transcribedText.trim().split(" ")
                if (words.size > 15) {
                    val history = words.dropLast(10).joinToString(" ")
                    val latest = words.takeLast(10).joinToString(" ")

                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))) {
                        append(history)
                        append(" ")
                    }
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)) {
                        append(latest)
                    }
                } else {
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)) {
                        append(transcribedText)
                    }
                }
            }

            Text(
                text = annotatedText,
                style = MaterialTheme.typography.displaySmall.copy(
                    lineHeight = 44.sp,
                    letterSpacing = (-0.5).sp
                ),
                textAlign = TextAlign.Start
            )

            if (partialTranscription.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = partialTranscription,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            lineHeight = 44.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    // Animated Cursor
                    Box(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(width = 3.dp, height = 36.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorStateView(
    icon: ImageVector,
    title: String,
    message: String,
    actionText: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Custom Styled Action Button is actually the FAB below, 
        // but adding local retry if needed.
    }
}

@Composable
fun BottomActionSection(
    state: CommunicationEngineState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp, top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val fabIcon = when (state) {
            is CommunicationEngineState.Ready -> Icons.Default.Mic
            is CommunicationEngineState.Listening -> Icons.Default.Stop
            is CommunicationEngineState.Busy -> Icons.Outlined.Refresh
            is CommunicationEngineState.Offline -> Icons.Default.MicOff
        }

        val fabColor = when (state) {
            is CommunicationEngineState.Listening -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceVariant
        }

        val footerText = when (state) {
            is CommunicationEngineState.Ready -> stringResource(R.string.tap_to_start)
            is CommunicationEngineState.Listening -> stringResource(R.string.tap_to_stop)
            is CommunicationEngineState.Busy -> stringResource(R.string.mic_in_use)
            is CommunicationEngineState.Offline -> stringResource(R.string.retry_connection)
        }

        FloatingActionButton(
            onClick = {
                when (state) {
                    is CommunicationEngineState.Ready -> onStart()
                    is CommunicationEngineState.Listening -> onStop()
                    else -> onRetry()
                }
            },
            modifier = Modifier.size(84.dp),
            shape = CircleShape,
            containerColor = fabColor,
            contentColor = if (state is CommunicationEngineState.Listening)
                MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface,
            elevation = TopAppBarDefaults.centerAlignedTopAppBarColors().containerColor.let {
                // Using a simpler elevation since I can't easily get the value here
                androidx.compose.material3.FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
            }
        ) {
            Icon(
                imageVector = fabIcon,
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = footerText.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp
        )
    }
}

private data class BadgeConfig(
    val label: String,
    val containerColor: Color,
    val dotColor: Color,
)

// --- Previews ---

@Preview(showBackground = true)
@Preview(showBackground = true, locale = "ar")
@PreviewLightDark
@Composable
fun PreviewCommunicationReady() {
    HearForMeTheme {
        CommunicationScreenContent(
            uiState = CommunicationUiState(),
            engineState = CommunicationEngineState.Ready,
            onBack = {},
            onStartListening = {},
            onStopListening = {},
            onRetry = {},
            onClearTranscription = {}
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, locale = "ar")
@PreviewLightDark
@Composable
fun PreviewCommunicationListening() {
    HearForMeTheme {
        CommunicationScreenContent(
            uiState = CommunicationUiState(
                transcribedText = "Hello world this is a test of the live transcription system",
                partialTranscription = "with a partial"
            ),
            engineState = CommunicationEngineState.Listening,
            onBack = {},
            onStartListening = {},
            onStopListening = {},
            onRetry = {},
            onClearTranscription = {}
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, locale = "ar")
@PreviewLightDark
@Composable
fun PreviewCommunicationBusy() {
    HearForMeTheme {
        CommunicationScreenContent(
            uiState = CommunicationUiState(),
            engineState = CommunicationEngineState.Busy,
            onBack = {},
            onStartListening = {},
            onStopListening = {},
            onRetry = {},
            onClearTranscription = {}
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, locale = "ar")
@PreviewLightDark
@Composable
fun PreviewCommunicationOffline() {
    HearForMeTheme {
        CommunicationScreenContent(
            uiState = CommunicationUiState(),
            engineState = CommunicationEngineState.Offline,
            onBack = {},
            onStartListening = {},
            onStopListening = {},
            onRetry = {},
            onClearTranscription = {}
        )
    }
}
