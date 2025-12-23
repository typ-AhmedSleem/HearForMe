package com.typ.hearforme.presentation.alerts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typ.hearforme.domain.model.SoundEvent
import com.typ.hearforme.domain.model.SoundType
import com.typ.hearforme.presentation.dashboard.DashboardViewModel
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun AlertOverlay(
    event: SoundEvent,
    onDismiss: () -> Unit,
    viewModel: DashboardViewModel = koinViewModel(),
) {
    if (event.type.priority == SoundType.Priority.CRITICAL) {
        LaunchedEffect(event) {
            while (true) {
                viewModel.triggerAlertFeedback(event)
                delay(3000)
            }
        }
    }

    DisposableEffect(Unit) {
        // todo: Style the StatusBar based on color of the background
        onDispose {
            // todo: Reset StatusBar style
        }
    }

    rememberInfiniteTransition(label = "FlashTransition")
    val typeColor = Color(event.type.color)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(typeColor),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = true, // It's always true while AlertOverlay is in the composition
            enter = scaleIn(),
            exit = scaleOut()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                // Urgent Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.padding(bottom = 48.dp)
                ) {
                    Text(
                        "${event.type.priority.name} ALERT",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }

                // Emoji/Icon
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(event.type.emoji, fontSize = 100.sp)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = event.type.displayName,
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Detected in your immediate vicinity.\nPlease check at once.",
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    lineHeight = 26.sp
                )

                Spacer(modifier = Modifier.height(80.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) {
                        Text("Dismiss", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    /*Surface(
                        modifier = Modifier.size(64.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        onClick = {} // todo: make use of this !!
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🔕", fontSize = 24.sp)
                        }
                    }*/
                }
            }
        }
    }
}
