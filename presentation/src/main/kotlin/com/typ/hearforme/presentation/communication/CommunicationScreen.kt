package com.typ.hearforme.presentation.communication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.typ.hearforme.designsystem.theme.PrimaryBlue
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunicationScreen(
    onBack: () -> Unit,
    viewModel: CommunicationViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Live Transcribe", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearTranscription() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Transcription area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column {
                    Text(
                        text = if (uiState.transcribedText.isEmpty() && uiState.partialTranscription.isEmpty())
                            "Tap the mic and start speaking Arabic..." else uiState.transcribedText,
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (uiState.transcribedText.isEmpty())
                            MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start
                    )
                    if (uiState.partialTranscription.isNotEmpty()) {
                        Text(
                            text = uiState.partialTranscription,
                            style = MaterialTheme.typography.headlineSmall,
                            color = PrimaryBlue.copy(alpha = 0.6f),
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Mic button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                FloatingActionButton(
                    onClick = {
                        if (uiState.isListening) viewModel.stopListening() else viewModel.startListening()
                    },
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    containerColor = if (uiState.isListening) Color.Red else PrimaryBlue,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = if (uiState.isListening) {
                            Icons.Default.Call
                        } else {
                            Icons.Outlined.Call
                        },
                        contentDescription = "Mic",
                        modifier = Modifier.size(40.dp)
                    )
                }

                if (uiState.isListening) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Listening...",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                    )
                }
            }
        }
    }

    // * Start listening when the screen is visible
    LaunchedEffect(Unit) {
        if (!uiState.isListening) {
            viewModel.startListening()
        }
    }
}
