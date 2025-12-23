package com.typ.hearforme.presentation.communication

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var inputText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Live Transcribe", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
        ) {
            // Transcription area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column {
                    Text(
                        text = if (uiState.transcribedText.isEmpty() && uiState.partialTranscription.isEmpty())
                            "Tap the mic and start speaking Arabic..." else uiState.transcribedText,
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (uiState.transcribedText.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
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

            Spacer(modifier = Modifier.height(24.dp))

            // Text-to-Speech input
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Type to speak aloud...") },
                shape = RoundedCornerShape(32.dp),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (inputText.isNotEmpty()) {
                                viewModel.speak(inputText)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotEmpty()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Speak", tint = PrimaryBlue)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedBorderColor = PrimaryBlue,
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Mic button
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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
                        // todo: use proper animated icons later.
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
                    Text(
                        "Listening...",
                        modifier = Modifier.padding(top = 100.dp),
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                    )
                }
            }
        }
    }
}
