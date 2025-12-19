package com.typ.hearforme.presentation.welcome

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typ.hearforme.designsystem.theme.PrimaryBlue
import com.typ.hearforme.designsystem.theme.PrimaryGradient

@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Logo & Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            // Placeholder for logo icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(PrimaryBlue, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("👂", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Hear for Me",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Central Graphic (Canvas)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            WelcomeGraphics()
        }

        // Text Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Text(
                text = "See the sounds\naround you",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 36.sp,
                    lineHeight = 44.sp
                ),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Your phone listens for alarms, voices, and sirens, notifying you instantly.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Get Started", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { /* Login */ }) {
                Text(
                    "Log in to existing account",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WelcomeGraphics() {
    Box(contentAlignment = Alignment.Center) {
        // Concentric Circles
        Canvas(modifier = Modifier.size(300.dp)) {
            this.center
            drawCircle(
                color = PrimaryBlue.copy(alpha = 0.05f),
                radius = 150.dp.toPx(),
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = PrimaryBlue.copy(alpha = 0.1f),
                radius = 120.dp.toPx(),
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = PrimaryBlue.copy(alpha = 0.2f),
                radius = 90.dp.toPx(),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Central Image Placeholder (Circular)
        Surface(
            modifier = Modifier.size(140.dp),
            shape = RoundedCornerShape(70.dp),
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(PrimaryGradient)),
                contentAlignment = Alignment.Center
            ) {
                // Here we would place an image. Using a placeholder for now.
                Text("🌊", fontSize = 60.sp)
            }
        }

        // Floating Labels (Simulation)
        // Note: In a real app, these would be positioned relative to the center
        // Using offsets for quick implementation

        Box(
            modifier = Modifier
                .offset(x = (-80).dp, y = (-100).dp)
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .shadow(2.dp, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ALERT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("Visual", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Box(
            modifier = Modifier
                .offset(x = (80).dp, y = (60).dp)
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .shadow(2.dp, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("FEEL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("Haptics", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
